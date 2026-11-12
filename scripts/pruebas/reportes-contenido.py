#!/usr/bin/env python3
"""Contrasta texto de PDFs Jasper reales con consultas SQL independientes del test Java."""
from decimal import Decimal
from pathlib import Path
import re
root=Path('work/evidencias')
log=(root/'reportes-java.log').read_text()
values={m[0]:(int(m[1]),Decimal(m[2])) for m in re.findall(r'SQL (\w+) cantidad=(\d+) monto=([\d.]+)',log)}
def text(name): return (root/'reportes'/(name+'.txt')).read_text()
def money(x): return f'{x:,.2f}'
assert re.search(r'Total validado S/\s+'+re.escape(money(values['INGRESOS'][1]))+r'\b',text('INGRESOS')),'Total general PDF difiere de SQL'
assert re.search(r'Total validado S/\s+'+re.escape(money(values['INGRESOS_FILTRO'][1]))+r'\b',text('INGRESOS-FILTRO')),'Total filtrado PDF difiere de SQL'
assert re.search(r'Observaciones pendientes:\s+'+str(values['OBSERVADOS'][0])+r'\b',text('OBSERVADOS')),'Cantidad de observaciones difiere'
assert re.search(r'Total completadas\s+'+str(values['FINALIZADA'][0])+r'\b',text('VACANTES')),'Cantidad FINALIZADA difiere'
assert '0.00' in text('INGRESOS-VACIO'),'Reporte vacío no muestra total cero'
assert 'compartido' in text('VACANTES'),'Falta advertencia del alcance del cupo'
assert 'FIN_OBSERVACION_EXTENSA' in text('OBSERVACION-EXTENSA'),'Se truncó la descripción extensa'
print('PASS contenido PDF: total general, filtro, observados, FINALIZADA, vacío, cupo compartido y descripción extensa')
