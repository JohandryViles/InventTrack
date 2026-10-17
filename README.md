# InventTrack

Base Android de la etapa 2, en el repositorio original del equipo.

## Ejecutar

Abrir este directorio en Android Studio, sincronizar Gradle y ejecutar `app` en Android 8.0 (API 26) o superior. Usar JDK 17 o 21 para Gradle y tener instalado Android SDK 35.

```sh
./gradlew :app:assembleDebug :app:lintDebug
```

## Organización y colaboración

- `ui/theme`: colores claros/oscuros, Shapes y Typography de Material 3.
- `ui/components`: contenedor flexible, desplazable y con ancho máximo.
- `ui/screens`: Movimiento, Análisis y puntos de integración del equipo.
- `navigation/Routes.kt`: las doce rutas, definidas únicamente aquí.
- `navigation/AppNavigation.kt`: NavHost, NavController y NavigationBar.

Las pantallas de otros integrantes usan `IntegrationScreen` de manera provisional. Sustituir su contenido en el destino correspondiente sin cambiar el nombre de la ruta. Login permite entrar explícitamente en modo demo; no implementa autenticación real.

No se usan ViewModels, repositorios ni capa de datos. Los datos mock viven dentro de los composables. El formulario conserva su estado durante recreaciones y navegación entre pestañas. Las estadísticas son ejemplos independientes y no se recalculan con los movimientos.

La interfaz se construye exclusivamente con Compose. Los únicos XML son el manifiesto que requiere Android y el drawable vectorial del launcher, que el sistema necesita antes de iniciar Compose.

## Rutas y revisión manual

1. Inicio de la app: Splash → Login. Atrás desde Login no debe mostrar Splash.
2. Login → Crear cuenta → Atrás vuelve a Login.
3. Entrar en modo demo → Inicio. Atrás no debe mostrar Login ni Splash.
4. Recorrer Inicio, Productos, Movim., Chatbot y Más; repetir pestañas sin duplicar destinos.
5. Productos → Nuevo producto y Categorías; Más → Categorías, Historial y Análisis. Usar Atrás en cada destino.
6. Movimiento: probar producto vacío, cero, negativos, texto, cantidad excesiva y salida superior al stock. Registrar entradas y salidas válidas, elegir fecha, escribir observación y comprobar la confirmación y el stock.
7. Análisis: cambiar Semanal/Mensual/Anual; comprobar barras, dona, leyendas y tarjetas.
8. Repetir en pantalla pequeña, horizontal, tablet, modo oscuro y texto ampliado. Comprobar scroll y teclado.
9. Más → Cerrar sesión demo; Atrás no debe volver a pantallas internas.

Todos los destinos tienen un acceso desde la interfaz; los formularios auxiliares regresan con Atrás del sistema. El registro de movimientos y el análisis corresponden a las pantallas 8 y 11.

## Entrega

Cada integrante debe revisar y realizar sus propios commits con su identidad en este mismo repositorio. La base de navegación debe subirse antes de integrar las otras pantallas. Cada integrante debe entregar además su Word en el aula virtual; el código no sustituye esa entrega.
