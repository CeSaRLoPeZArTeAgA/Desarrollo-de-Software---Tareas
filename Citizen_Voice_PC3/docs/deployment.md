# Despliegue paso a paso - Citizen Voice PC3 Java

## 1. Ubicacion del proyecto

El proyecto puede estar en cualquier ruta. Ejemplo valido:

```text
D:\CICLO 2026-1 UNI\Desarrollo-de-Software---Tareas\Citizen_Voice_PC3
```

La carpeta importante es `Citizen_Voice_PC3`. Dentro debe verse:

```text
build/
data/
docs/
scripts/
src/
Dockerfile
docker-compose.yml
pom.xml
README.md
```

## 2. Abrir en Visual Studio Code

Abra VS Code y seleccione:

```text
File -> Open Folder -> Citizen_Voice_PC3
```

Abra la terminal integrada:

```text
Terminal -> New Terminal
```

## 3. Comprobar Java

```powershell
java -version
javac -version
```

Debe existir `javac`. Si no existe, instale JDK 17 o superior.

## 4. Compilar

Desde la raiz del proyecto:

```powershell
.\scripts\compile.bat
```

Si esta dentro de `scripts`, ejecute:

```powershell
.\compile.bat
```

El script funciona aunque la ruta tenga espacios.

## 5. Ejecutar pruebas

Desde la raiz:

```powershell
.\scripts\run-tests.bat
```

Resultado esperado:

```text
TESTS OK: 27 verificaciones generales superadas
```

## 6. Generar base general de 100 000 personas

```powershell
.\scripts\seed-registry.bat
```

Este proceso puede tardar varios minutos porque genera 100 000 pares RSA de 2048 bits.

Archivos creados:

```text
data\private_registry.db
data\public_registry.db
```

## 7. Crear propuesta

```powershell
java -cp "build\classes" pe.edu.uni.fc.cc.citizenvoice.app.CitizenVoiceCli create-from-template --template transparencia-digital
```

Normalmente se crea la propuesta con ID 1.

## 8. Ejecutar flujo PC3

```powershell
.\scripts\run-pc3-demo.bat 1 25000
```

Esto hace:

1. toma 25 000 personas aleatorias desde el padron de 100 000;
2. firma digitalmente con llave privada RSA;
3. verifica con llave publica RSA;
4. registra firmas validas;
5. congela el expediente con SHA-256;
6. genera el envio simulado al Congreso.

## 9. Iniciar servidor HTTP

```powershell
.\scripts\start-server.bat
```

Abrir en navegador:

```text
http://127.0.0.1:8080
```

## 10. Prueba rapida para no esperar las 25 000 firmas

Solo para verificar funcionamiento:

```powershell
.\scripts\seed-registry.bat 20 512 2
java -cp "build\classes" pe.edu.uni.fc.cc.citizenvoice.app.CitizenVoiceCli create-from-template --template transparencia-digital
.\scripts\run-pc3-demo.bat 1 5
```

Para la entrega formal use:

```text
100 000 personas
25 000 firmas
RSA 2048 bits
```
