#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/../.."
: "${ADMITU_DB_PROPERTIES:?Indique un archivo privado MySQL del ensayo autorizado}"
: "${ADMITU_PRUEBAS_ESCRITURA:?Debe valer true y el host/schema deben estar verificados}"
if [ "$ADMITU_PRUEBAS_ESCRITURA" != true ]; then
  echo 'No se ejecutaron escrituras. Verifique el destino propio y active ADMITU_PRUEBAS_ESCRITURA=true.' >&2
  exit 1
fi
mvn -B -f backend/AdmitU/pom.xml package
admitu_jdbc_jar="${ADMITU_JDBC_JAR:-${MAVEN_USER_HOME:-$HOME/.m2}/repository/com/mysql/mysql-connector-j/26.7.0/mysql-connector-j-26.7.0.jar}"
test -f "$admitu_jdbc_jar" || { echo 'Configure ADMITU_JDBC_JAR con el JAR MySQL disponible.' >&2; exit 1; }
java -cp "backend/AdmitU/admitu/target/test-classes:backend/AdmitU/admitu_domain/target/classes:backend/AdmitU/admitu_dbmanager/target/classes:backend/AdmitU/admitu_dao/target/classes:backend/AdmitU/admitu_business_logic/target/classes:$admitu_jdbc_jar" pe.edu.pucp.admitu.pruebas.PruebasReglasLab12
