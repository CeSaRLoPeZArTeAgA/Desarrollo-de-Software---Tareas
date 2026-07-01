GUIA DE DESPLIEGUE - UNI HUELLITAS / PC4
================================================

Proyecto: Sistema de mascotas UNI Huellitas
Tecnologia: Python + FastAPI + Jinja2 + SQLite
Sistema recomendado: Windows 10/11 con PowerShell
Python recomendado: Python 3.12 o superior

------------------------------------------------
1. CONTENIDO QUE DEBE RECIBIR LA OTRA PERSONA
------------------------------------------------

La otra persona debe recibir la carpeta completa del proyecto o el archivo ZIP:

    UNI_Huellitas_PawlyStyle_FUNCIONAL.zip

Dentro del proyecto deben existir, como minimo, estas carpetas y archivos:

    PC4/
    ├── data/
    ├── scripts/
    ├── src/
    ├── tests/
    ├── requirements.txt
    ├── requirements-dev.txt
    ├── Dockerfile
    ├── docker-compose.yml
    └── README.md

Si el proyecto esta comprimido, primero debe descomprimirse en una ruta sin caracteres raros.
Ruta recomendada:

    D:\CICLO 2026-1 UNI\Desarrollo-de-Software---Tareas\PC4

Tambien puede usarse una ruta mas simple:

    C:\PC4

------------------------------------------------
2. ABRIR POWERSHELL EN LA CARPETA DEL PROYECTO
------------------------------------------------

Abrir PowerShell y entrar a la carpeta del proyecto:

    cd "D:\CICLO 2026-1 UNI\Desarrollo-de-Software---Tareas\PC4"

Si se usa otra ruta, reemplazar la ruta anterior. Por ejemplo:

    cd "C:\PC4"

Verificar que existan los archivos principales:

    ls

Debe verse algo similar:

    data
    scripts
    src
    tests
    requirements.txt
    requirements-dev.txt

------------------------------------------------
3. CREAR EL ENTORNO VIRTUAL
------------------------------------------------

Crear un entorno virtual llamado .venv_pc4:

    python -m venv .venv_pc4

IMPORTANTE:
No presionar CTRL + C mientras se crea el entorno. Esperar hasta que termine.

Si aparece un error indicando que python no existe, probar:

    py -m venv .venv_pc4

------------------------------------------------
4. HABILITAR SCRIPTS EN POWERSHELL
------------------------------------------------

Ejecutar:

    Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass

Esto solo habilita scripts en la terminal actual.

------------------------------------------------
5. ACTIVAR EL ENTORNO VIRTUAL
------------------------------------------------

Activar el entorno:

    .\.venv_pc4\Scripts\Activate.ps1

Debe aparecer algo asi al inicio de la linea:

    (.venv_pc4) PS C:\PC4>

Si aparece (.venv_pc4), el entorno esta activo.

------------------------------------------------
6. ACTUALIZAR PIP
------------------------------------------------

Ejecutar:

    python -m pip install --upgrade pip

------------------------------------------------
7. INSTALAR DEPENDENCIAS
------------------------------------------------

Instalar dependencias de ejecucion y pruebas:

    pip install -r requirements-dev.txt

Si solo se quiere ejecutar el sistema sin pruebas:

    pip install -r requirements.txt

Pero para entrega academica se recomienda usar requirements-dev.txt.

------------------------------------------------
8. CONFIGURAR PYTHONPATH
------------------------------------------------

En PowerShell ejecutar:

    $env:PYTHONPATH="src"

Esto permite que Python encuentre el paquete principal pet_alerts.

------------------------------------------------
9. CREAR O REINICIAR LA BASE DE DATOS DE DEMO
------------------------------------------------

Ejecutar:

    python scripts\seed_demo.py

Resultado esperado:

    Base de datos inicializada: data\mascotas_uni.db
    Reportes: 4
    Cuidadores: 3
    Criaderos certificados: 3

Este comando crea datos de ejemplo para que la aplicacion sea funcional.

------------------------------------------------
10. EJECUTAR PRUEBAS UNITARIAS
------------------------------------------------

Ejecutar:

    pytest -q

Resultado esperado:

    .................. [100%]
    18 passed

Puede aparecer una advertencia de StarletteDeprecationWarning.
Esa advertencia no impide ejecutar la aplicacion.

------------------------------------------------
11. LEVANTAR LA APLICACION
------------------------------------------------

Ejecutar:

    python -m uvicorn pet_alerts.main:app --reload --host 0.0.0.0 --port 8000

Resultado esperado:

    Uvicorn running on http://0.0.0.0:8000
    Application startup complete.

IMPORTANTE:
No escribir nada despues de --port 8000.
El comando debe terminar exactamente en:

    --port 8000

------------------------------------------------
12. ABRIR LA APLICACION EN EL NAVEGADOR
------------------------------------------------

Abrir:

    http://localhost:8000

Documentacion de la API:

    http://localhost:8000/docs

Paginas principales:

    http://localhost:8000/
    http://localhost:8000/adopcion
    http://localhost:8000/perdidos-y-encontrados
    http://localhost:8000/buscar-por-imagen
    http://localhost:8000/cuidadores
    http://localhost:8000/publicar-anuncio

------------------------------------------------
13. SECUENCIA COMPLETA PARA COPIAR Y PEGAR
------------------------------------------------

Usar esta secuencia si el proyecto ya esta descomprimido:

    cd "D:\CICLO 2026-1 UNI\Desarrollo-de-Software---Tareas\PC4"

    python -m venv .venv_pc4

    Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass

    .\.venv_pc4\Scripts\Activate.ps1

    python -m pip install --upgrade pip

    pip install -r requirements-dev.txt

    $env:PYTHONPATH="src"

    python scripts\seed_demo.py

    pytest -q

    python -m uvicorn pet_alerts.main:app --reload --host 0.0.0.0 --port 8000

Luego abrir:

    http://localhost:8000

------------------------------------------------
14. COMO EJECUTAR EL PROYECTO OTRO DIA
------------------------------------------------

Cuando el entorno ya fue creado antes, no volver a crear el entorno.
Solo ejecutar:

    cd "D:\CICLO 2026-1 UNI\Desarrollo-de-Software---Tareas\PC4"

    Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass

    .\.venv_pc4\Scripts\Activate.ps1

    $env:PYTHONPATH="src"

    python -m uvicorn pet_alerts.main:app --reload --host 0.0.0.0 --port 8000

Luego abrir:

    http://localhost:8000

------------------------------------------------
15. COMO DETENER EL SERVIDOR
------------------------------------------------

En la terminal donde corre uvicorn, presionar:

    CTRL + C

------------------------------------------------
16. ERRORES COMUNES Y SOLUCIONES
------------------------------------------------

ERROR 1:
    source no se reconoce como comando

Causa:
    source es de Linux/Mac.

Solucion en Windows PowerShell:

    .\.venv_pc4\Scripts\Activate.ps1

------------------------------------------------

ERROR 2:
    El termino Activate.ps1 no se reconoce

Causa:
    El entorno virtual no existe o se creo en otra carpeta.

Solucion:
    Verificar que estas en la carpeta PC4:

        pwd

    Crear el entorno:

        python -m venv .venv_pc4

    Activar:

        .\.venv_pc4\Scripts\Activate.ps1

------------------------------------------------

ERROR 3:
    Acceso denegado a python.exe dentro de .venv

Causa:
    Hay un proceso de Python usando el entorno.

Solucion:

    taskkill /F /IM python.exe
    taskkill /F /IM pythonw.exe

Luego volver a intentar.

------------------------------------------------

ERROR 4:
    No module named pet_alerts

Causa:
    No se configuro PYTHONPATH.

Solucion:

    $env:PYTHONPATH="src"

Luego ejecutar otra vez:

    python -m uvicorn pet_alerts.main:app --reload --host 0.0.0.0 --port 8000

------------------------------------------------

ERROR 5:
    Invalid value for '--port'

Causa:
    Se pego texto despues de 8000.

Comando correcto:

    python -m uvicorn pet_alerts.main:app --reload --host 0.0.0.0 --port 8000

------------------------------------------------

ERROR 6:
    El puerto 8000 esta ocupado

Solucion 1:
    Cerrar el servidor anterior con CTRL + C.

Solucion 2:
    Usar otro puerto:

    python -m uvicorn pet_alerts.main:app --reload --host 0.0.0.0 --port 8001

Abrir:

    http://localhost:8001

------------------------------------------------
17. DESPLIEGUE CON DOCKER
------------------------------------------------

Si la otra persona tiene Docker Desktop instalado, puede ejecutar:

    cd "D:\CICLO 2026-1 UNI\Desarrollo-de-Software---Tareas\PC4"

    docker compose up --build

Luego abrir:

    http://localhost:8000

Para detener:

    CTRL + C

Para apagar contenedores:

    docker compose down

------------------------------------------------
18. RECOMENDACION PARA ENTREGAR A OTRA PERSONA
------------------------------------------------

Enviar:

    1. UNI_Huellitas_PawlyStyle_FUNCIONAL.zip
    2. Este archivo GUIA_DESPLIEGUE_UNI_HUELLITAS.txt
    3. Indicar que use PowerShell y Python 3.12 o superior

La persona debe:
    1. Descomprimir el ZIP.
    2. Entrar a la carpeta PC4.
    3. Seguir la seccion 13.
    4. Abrir http://localhost:8000

------------------------------------------------
FIN DE LA GUIA
------------------------------------------------
