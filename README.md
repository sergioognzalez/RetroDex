# RetroDex JavaFX - Testing y CI/CD

Este repositorio contiene el proyecto original RetroDex Android y, para esta entrega, un modulo especifico JavaFX llamado `retrodex-javafx`. La evaluacion de testing se realiza sobre ese modulo JavaFX porque la rubrica pide TestFX, pruebas headless y cobertura con JaCoCo.

## Fase 1: Fundamentos y diseno

### Por que el Testing E2E es critico en interfaces pero mas costoso que el unitario

El testing unitario comprueba partes pequenas y aisladas del programa, como una funcion que valida datos, ordena personajes o filtra una lista. Es rapido, barato de ejecutar y normalmente sus fallos son faciles de localizar, porque solo prueba una pieza concreta de la logica.

El testing E2E, o End-to-End, es critico en interfaces porque comprueba la aplicacion desde el punto de vista real del usuario. En vez de probar una funcion aislada, simula acciones como abrir la app, pulsar botones, escribir en formularios, navegar entre pantallas, usar filtros y comprobar que el resultado visible es correcto. Este tipo de prueba detecta errores que el unitario no ve, como fallos de integracion entre la vista, la logica de negocio, los componentes graficos, la navegacion, la asincronia de la interfaz o el estado final mostrado al usuario.

Sin embargo, el testing E2E es mas costoso que el unitario porque necesita arrancar toda la aplicacion y ejecutar la interfaz en un entorno grafico o headless. Tambien tarda mas, requiere mas infraestructura y puede ser mas fragil ante cambios en la UI, como textos, ids, botones, estructura visual o tiempos de carga. Por eso una estrategia razonable es tener muchos tests unitarios para la logica y menos tests E2E, pero centrados en los flujos principales del usuario.

### Diferencia entre Continuous Delivery y Continuous Deployment

Continuous Delivery significa que cada cambio que supera build, tests y validaciones queda listo para desplegarse, pero el paso final a produccion necesita una aprobacion manual. El pipeline automatiza la preparacion de la entrega, pero una persona decide cuando publicar.

Continuous Deployment elimina esa aprobacion manual. En este caso, si el cambio supera correctamente todas las fases del pipeline, como compilacion, tests unitarios, cobertura y pruebas E2E, se despliega automaticamente en produccion.

La diferencia principal esta en el disparador final: en Continuous Delivery hay decision humana antes del despliegue; en Continuous Deployment el despliegue tambien esta completamente automatizado.

## Fase 2: Desarrollo y testing

### Logica de negocio

La logica de negocio esta separada de la interfaz en:

`retrodex-javafx/src/main/java/com/sergio/retrodexjavafx/core/CharacterCatalogService.java`

Esta clase implementa:

- Alta de personajes.
- Edicion de personajes.
- Eliminacion de personajes.
- Busqueda por texto.
- Filtro por decada.
- Filtro por categoria.
- Ordenacion por nombre y por decada.
- Generacion del texto para compartir.

La interfaz JavaFX esta en:

`retrodex-javafx/src/main/java/com/sergio/retrodexjavafx/RetroDexFxApp.java`

### Unit tests y cobertura JaCoCo

Los unit tests estan en:

`retrodex-javafx/src/test/java/com/sergio/retrodexjavafx/core/CharacterCatalogServiceTest.java`

Comandos:

```bash
./gradlew :retrodex-javafx:test
./gradlew :retrodex-javafx:jacocoTestReport
./gradlew :retrodex-javafx:jacocoTestCoverageVerification
```

Resultado verificado:

- Tests unitarios correctos.
- Cobertura JaCoCo verificada al 100% sobre la logica de negocio.
- El minimo exigido por la tarea era 50%.

Informe generado:

`retrodex-javafx/build/reports/jacoco/test/html/index.html`

### Pruebas E2E con TestFX

Los tests E2E estan en:

`retrodex-javafx/src/e2eTest/java/com/sergio/retrodexjavafx/RetroDexFxE2ETest.java`

Se han creado 3 casos independientes que simulan acciones reales de usuario:

- Buscar a Goku, abrir su detalle, limpiar la busqueda, filtrar por decada, cambiar orden y abrir a Mario.
- Crear un personaje, elegir decada y categoria, guardar, editarlo, compartirlo y eliminarlo.
- Abrir ajustes, abrir acerca de, volver al catalogo, validar un formulario vacio, buscar a Sonic y compartirlo.

Cada prueba termina con aserciones explicitas sobre el estado de la interfaz, como textos de detalle, mensajes de estado o contenido generado para compartir.

Comando:

```bash
./gradlew :retrodex-javafx:e2eTest
```

En GitHub Actions se ejecutan en modo headless mediante `xvfb-run`, que permite lanzar JavaFX/TestFX en un runner Linux sin pantalla fisica.

## Fase 3: Automatizacion con GitHub Actions

El workflow esta en:

`.github/workflows/main.yml`

Jobs configurados:

- `Build`: configura Java y Gradle, instala dependencias y compila la aplicacion JavaFX.
- `Test`: ejecuta unit tests, genera JaCoCo y verifica cobertura.
- `E2E`: ejecuta TestFX en entorno headless con `xvfb-run`.
- `Deploy`: solo se ejecuta si `Build`, `Test` y `E2E` han terminado correctamente.

El despliegue se prepara con GitHub Pages, que actua como servidor externo de publicacion. El job genera un ZIP distribuible de la aplicacion JavaFX y publica una pagina con el enlace de descarga. El job `deploy` depende de `build`, `test` y `e2e`, y solo se ejecuta si los tres terminan correctamente.
