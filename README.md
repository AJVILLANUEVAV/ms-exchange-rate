# ms-exchange-rate

Microservicio REST para consultar el tipo de cambio diario y registrar las
consultas por DNI. Está implementado con Java 21 y Quarkus 3.27.6.1.

## Funcionalidad

- Expone `GET /api/exchange-rate?dni={dni}`.
- Consulta la API externa `https://free.e-api.net.pe/tipo-cambio/today.json`.
- Limita a 10 las consultas diarias por DNI. Al superar el límite responde con
  HTTP `422 Unprocessable Entity`.
- Registra cada consulta exitosa en H2, guardando DNI, fecha y valor de venta.
- Rechaza solicitudes sin DNI con HTTP `400 Bad Request`.

La base H2 está configurada en memoria (`jdbc:h2:mem:testdb`), por lo que los
registros se pierden al detener o reiniciar la aplicación.

## Tecnologías y estructura

- **Quarkus REST y Jackson**: endpoint HTTP y respuesta JSON.
- **Quarkus REST Client**: comunicación con el proveedor externo.
- **Hibernate ORM con Panache**: persistencia de registros.
- **H2**: base de datos en memoria.
- **Maven Wrapper**: ejecución y compilación con Maven.

```text
src/main/java/com/nttdata/
├── controller/  ExchangeRateResource: endpoint REST
├── service/     ExchangeRateService: límite y flujo de negocio
├── proxy/       Cliente externo y DTO de respuesta
├── entity/      ExchangeLog: entidad JPA
└── repository/  ExchangeLogRepository: conteo y persistencia
src/main/resources/
└── application.properties: base H2 y configuración REST Client
src/main/docker/               Dockerfiles para JVM y modo nativo
```

## Requisitos previos

- JDK 21 instalado y `JAVA_HOME` configurado para apuntar al JDK 21.
- Acceso de red a `https://free.e-api.net.pe`.

Verifica la versión de Java desde PowerShell:

```powershell
java -version
```

Debe mostrar Java 21. Maven se ejecuta mediante el wrapper incluido; no es
necesario instalarlo por separado.

## Levantar en modo desarrollo

Desde la carpeta raíz del proyecto, abre PowerShell y ejecuta:

```powershell
.\mvnw.cmd quarkus:dev
```

Quarkus inicia en `http://localhost:8080` y permanece en primer plano. Para
detenerlo, usa `Ctrl+C`. La Dev UI está disponible en
`http://localhost:8080/q/dev/`.

## Probar con Postman

1. Crea una petición `GET`.
2. Usa esta URL:

   ```text
   http://localhost:8080/api/exchange-rate?dni=12345678
   ```

3. Envía la petición sin body.

Una respuesta exitosa (`200 OK`) contiene los campos del proveedor, por ejemplo:

```json
{
  "fecha": "2026-10-19",
  "sunat": 3.427,
  "compra": 3.423,
  "venta": 3.431
}
```

Si se omite `dni`, el endpoint responde `400`. Al alcanzar 10 consultas
exitosas ese mismo día para un DNI, responde `422` con un mensaje indicando que
se superó el límite. Los fallos de conexión con el proveedor o de persistencia
se registran en la consola de Quarkus.

## Configuración del cliente externo y TLS

La URL base y los timeouts se configuran en `application.properties`. El
proyecto actualmente configura el cliente `ExchangeRateProxy` con
`trust-all=true` y `verify-host=false` para permitir el acceso en el entorno de
desarrollo donde se observó un error de certificado (`PKIX path building
failed`). Estas opciones deshabilitan la validación TLS: **no deben utilizarse
en producción**. Para un despliegue real, configura un truststore con la cadena
de certificados válida y restaura la validación del certificado y del hostname.

## Empaquetar y ejecutar

Para construir el paquete JVM:

```powershell
.\mvnw.cmd package
```

Para ejecutar el artefacto empaquetado:

```powershell
java -jar target\quarkus-app\quarkus-run.jar
```

La compilación nativa es opcional y requiere GraalVM o una configuración de
build nativo en contenedor. Consulta la documentación de Quarkus antes de
habilitarla para el entorno objetivo.
