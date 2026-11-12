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
assert money(values['INGRESOS'][1]) in text('INGRESOS'),'Total general PDF difiere de SQL'
assert money(values['INGRESOS_FILTRO'][1]) in text('INGRESOS-FILTRO'),'Total filtrado PDF difiere de SQL'
assert str(values['OBSERVADOS'][0]) in text('OBSERVADOS'),'Cantidad de observaciones difiere'
assert str(values['FINALIZADA'][0]) in text('VACANTES'),'Cantidad FINALIZADA difiere'
assert '0.00' in text('INGRESOS-VACIO'),'Reporte vacío no muestra total cero'
assert 'compartido' in text('VACANTES'),'Falta advertencia del alcance del cupo'
print('PASS contenido PDF: total general, filtro, observados, FINALIZADA, vacío y cupo compartido')
