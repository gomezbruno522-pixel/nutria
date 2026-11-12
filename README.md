# NUTRIA Android — integración de pasos en segundo plano

Este proyecto integra la última versión HTML estable de NUTRIA en una aplicación Android (WebView) y conecta el botón Iniciar/Pausar con un servicio Android nativo.

## Cambios incluidos
- `TYPE_STEP_COUNTER` nativo de Android: no depende de que la pantalla web esté abierta.
- Servicio foreground con notificación persistente mientras el conteo está activo.
- El botón Iniciar activa el servicio y Pausar lo detiene.
- `AndroidSteps.getTodaySteps()` devuelve el acumulado diario al HTML.
- Los pasos quedan guardados en preferencias privadas de Android y se conservan al cerrar la interfaz.
- El receptor de reinicio intenta reactivar el servicio después de reiniciar el teléfono, si el usuario había dejado el conteo activo y Android permite el inicio.
- Se conservan los registros nutricionales en el almacenamiento local de la WebView.

## Compilación
Abrir la carpeta `NUTRIA_Android` en Android Studio, sincronizar Gradle y generar `Build > Build Bundle(s) / APK(s) > Build APK(s)`.

## Prueba obligatoria en el teléfono
1. Instalar la APK y conceder el permiso **Actividad física**.
2. Abrir NUTRIA y tocar **Iniciar**.
3. Confirmar que aparece la notificación de conteo activo.
4. Caminar unos minutos, bloquear la pantalla y seguir caminando.
5. Volver a NUTRIA: el total debe actualizarse con el acumulado nativo.
6. Cerrar la interfaz y volver a abrirla; comprobar que el total se conserva.
7. Probar Pausar y verificar que deja de aumentar.

## Límites y advertencias
- Este paquete es código fuente, no una APK compilada ni una prueba certificada en un Samsung real.
- Requiere un dispositivo con `Sensor.TYPE_STEP_COUNTER` y permiso de actividad física.
- Algunos fabricantes aplican ahorro de batería agresivo; puede ser necesario permitir actividad en segundo plano para NUTRIA.
- Android puede limitar el reinicio automático del servicio después de reiniciar el equipo. La prueba real en el teléfono es necesaria.
- El sensor entrega un acumulado del hardware; la primera lectura después de instalar o reiniciar establece una base y no reconstruye pasos anteriores a esa lectura.
- No desinstalar la app ni borrar sus datos durante la prueba, porque eso elimina el almacenamiento local de comidas/perfil.
- Para actualizar una instalación sin perder datos, conservar el mismo `applicationId` y firmar las versiones con la misma clave.
