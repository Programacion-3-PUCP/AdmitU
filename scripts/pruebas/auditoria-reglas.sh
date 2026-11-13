#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/../.."
: "${ADMITU_DB_PROPERTIES:?Indique archivo privado de la base de ensayo propia verificada}"
if [ "${ADMITU_PRUEBAS_ESCRITURA:-false}" != true ]; then
  echo 'Sin escrituras: requiere ADMITU_PRUEBAS_ESCRITURA=true.' >&2
  exit 1
fi
mvn -B -f backend/AdmitU/pom.xml package
admitu_jdbc_jar="${ADMITU_JDBC_JAR:-${MAVEN_USER_HOME:-$HOME/.m2}/repository/com/mysql/mysql-connector-j/26.7.0/mysql-connector-j-26.7.0.jar}"
test -f "$admitu_jdbc_jar"
java -cp "backend/AdmitU/admitu/target/test-classes:backend/AdmitU/admitu_domain/target/classes:backend/AdmitU/admitu_dbmanager/target/classes:backend/AdmitU/admitu_dao/target/classes:backend/AdmitU/admitu_business_logic/target/classes:$admitu_jdbc_jar" pe.edu.pucp.admitu.pruebas.PruebasAuditoriaPreaws
