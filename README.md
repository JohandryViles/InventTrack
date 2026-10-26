# InventTrack

App Android de inventario para farmacia (etapas 2 y 3), en el repositorio original del equipo.

## Ejecutar

Abrir este directorio en Android Studio, sincronizar Gradle y ejecutar `app` en Android 8.0 (API 26) o superior. Usar JDK 17 o 21 para Gradle y tener instalado Android SDK 35.

```sh
./gradlew :app:assembleDebug :app:lintDebug
./gradlew :app:testDebugUnitTest   # pruebas de repositorios y ViewModels
```

## Arquitectura (etapa 3: MVVM)

```
com.inventtrack
├── InventTrackApplication.kt   crea el AppContainer
├── di/                         AppContainer (repositorios) y AppViewModelProvider (Factory)
├── data/
│   ├── model/                  Producto, Categoria, Movimiento, Usuario, ChatMessage
│   ├── source/                 MockDataSource: único lugar con datos de ejemplo
│   └── repository/             interfaces Product, Category, Movement, Auth, Preferences, Chat
│       └── fake/               implementaciones en memoria (StateFlow + delay + Dispatchers.IO)
├── ui/
│   ├── settings/               referencia del patrón: SettingsRoute, SettingsScreen, SettingsViewModel
│   ├── session/                SessionViewModel (usuario de la sesión para la navegación)
│   ├── screens/                pantallas pendientes de migrar
│   ├── components/  theme/
└── navigation/                 Routes.kt y AppNavigation.kt
```

Flujo: `Screen (Compose) → ViewModel → Repository (interfaz) → Fake*Repository`. Cada fake guarda su estado en un `MutableStateFlow` privado (fuente única de verdad), simula latencia con `delay()` y ejecuta todo con `withContext`/`flowOn` en el dispatcher de IO que recibe del contenedor. Registrar un movimiento cambia el stock en `ProductRepository`, y todas las pantallas que lo observan se actualizan solas. No hay capa de dominio: no existe lógica de negocio compleja reutilizada entre varios ViewModels.

### Cómo migrar una pantalla

Tomar `ui/settings/` como ejemplo.

1. Crear `ui/<funcionalidad>/` y mover allí la pantalla.
2. `XViewModel(repos…)`: recibe sus repositorios **por constructor**, nunca `Repository()` adentro. Expone `val uiState: StateFlow<XUiState>` (`combine(...).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), …)`). Los eventos de una sola vez (navegar, cerrar diálogo) van en un `SharedFlow`.
3. Registrarlo en `di/AppViewModelProvider.kt`: `initializer { XViewModel(inventTrackContainer().productRepository) }`.
4. `XRoute(...)`: obtiene el ViewModel con `viewModel(factory = AppViewModelProvider.Factory)`, lee el estado con `collectAsStateWithLifecycle()`, escucha eventos con `ObserveAsEvents` y llama a `XScreen(uiState, onEvento…)`.
5. `XScreen` y sus hijos **no reciben el ViewModel**: solo datos y lambdas. El estado puramente visual (menú abierto, contraseña visible, texto de un diálogo) puede quedarse en `remember`. Los datos de negocio, no.
6. En `AppNavigation.kt`, cambiar solo el `composable(...)` de esa ruta para que llame a `XRoute`.
7. Borrar los `val mock…` y las listas fijas de la pantalla, porque los datos salen de los repositorios.

Login y Registro todavía abren la sesión a través de `SessionViewModel.startSession`. Cuando tengan su propio ViewModel, deben usar `AuthRepository.login`/`register`. La cuenta demo es `demo@inventtrack.com` / `demo123`; cualquier otro correo válido entra en modo demo.

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
