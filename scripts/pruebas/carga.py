#!/usr/bin/env python3
"""Carga GET HTTP concurrente. No equivale a 500 trámites completos de postulantes."""
import argparse
import asyncio
import json
import math
import os
from pathlib import Path
import ssl
import statistics
import time
from urllib.parse import urlsplit

async def main(a):
    url=urlsplit(a.url)
    if url.scheme not in ('http','https') or not url.hostname: raise SystemExit('Se requiere URL HTTP(S)')
    tokens=[]
    if a.tokens_file:
        tokens=[s.strip() for s in a.tokens_file.read_text().splitlines() if s.strip()]
        if len(tokens)!=a.users: raise SystemExit('Se requiere un token por usuario')
    elif os.environ.get('ADMITU_TEST_TOKEN'): tokens=[os.environ['ADMITU_TEST_TOKEN']]*a.users
    ready=asyncio.Event()
    target=(url.path or '/')+('?' + url.query if url.query else '')
    async def request(i):
        await ready.wait();start=time.perf_counter();writer=None
        try:
            reader,writer=await asyncio.wait_for(asyncio.open_connection(url.hostname,url.port or (443 if url.scheme=='https' else 80),ssl=ssl.create_default_context() if url.scheme=='https' else None),a.timeout)
            headers=f'GET {target} HTTP/1.1\r\nHost: {url.netloc}\r\nConnection: close\r\nAccept: application/json\r\n'
            if tokens: headers+='Authorization: Bearer '+tokens[i%len(tokens)]+'\r\n'
            writer.write((headers+'\r\n').encode('ascii'));await writer.drain()
            data=await asyncio.wait_for(reader.read(),a.timeout)
            status=int(data.split(b' ',2)[1]);return status,time.perf_counter()-start
        except (OSError,ValueError,TimeoutError,asyncio.TimeoutError): return 0,time.perf_counter()-start
        finally:
            if writer:
                writer.close()
                try: await writer.wait_closed()
                except OSError: pass
    tasks=[asyncio.create_task(request(i)) for i in range(a.users)]
    await asyncio.sleep(0);start=time.perf_counter();ready.set();results=await asyncio.gather(*tasks);elapsed=time.perf_counter()-start
    lat=sorted(t for _,t in results);statuses={str(k):sum(s==k for s,_ in results) for k in set(s for s,_ in results)}
    percentile=lambda q:lat[max(0,math.ceil(len(lat)*q)-1)]
    report={'scenario':'GET catálogo con conexiones HTTP simultáneas','path':target,'concurrency':a.users,'requests':len(results),'seconds':elapsed,'statuses':statuses,'latency_seconds':{'median':statistics.median(lat),'p95':percentile(.95),'p99':percentile(.99),'max':max(lat)},'all_http_200':all(s==200 for s,_ in results),'all_under_3_seconds':all(s==200 and t<=3 for s,t in results),'distinct_authenticated_tokens':len(set(tokens)),'full_applicant_workflows_tested':False,'scope':'Lectura de un endpoint local; no certifica RNF04 de 500 postulantes completando flujos.'}
    a.output.parent.mkdir(parents=True,exist_ok=True);a.output.write_text(json.dumps(report,indent=2,ensure_ascii=False)+'\n');print(json.dumps(report,indent=2,ensure_ascii=False))
    return 0 if report['all_http_200'] else 1

if __name__=='__main__':
    p=argparse.ArgumentParser(description=__doc__);p.add_argument('--url',default='http://127.0.0.1:8080/admitu_services/webresources/CarreraRS/listarTodasCarreras');p.add_argument('--users',type=int,default=500);p.add_argument('--timeout',type=float,default=30);p.add_argument('--tokens-file',type=Path);p.add_argument('--output',type=Path,default=Path('work/evidencias/carga.json'));a=p.parse_args()
    if not 1<=a.users<=500:p.error('--users debe estar entre1 y500')
    raise SystemExit(asyncio.run(main(a)))
