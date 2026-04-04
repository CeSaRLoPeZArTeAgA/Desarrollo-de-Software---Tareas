import os  # modulo para manejo de rutas del sistema operativo

#convierte una ruta tipo string en una lista de segmentos
def dividir_ruta(ruta):
    # caso especial: ruta raiz "/"
    if ruta == "/":
        return []

    # elimina "/" al inicio y final, luego separa por "/"
    return ruta.strip("/").split("/")


#determina si una transicion coincide con una ruta definida.
def coincide_ruta(ruta_definida, transicion):
    
    # dividir ambas rutas en segmentos
    partes_ruta = dividir_ruta(ruta_definida)
    partes_transicion = dividir_ruta(transicion)

    # si tienen distinta longitud, no coinciden
    if len(partes_ruta) != len(partes_transicion):
        return None

    # lista donde se almacenan los parametros capturados
    parametros = []

    # recorrer segmento a segmento
    for r, t in zip(partes_ruta, partes_transicion):

        # si el segmento de la ruta es un parametro (empieza con ":")
        if r.startswith(":"):
            parametros.append(t)  # guardar el valor correspondiente

        # si no es parametro, debe coincidir exactamente
        elif r != t:
            return None  # no coincide la ruta

    # si llega aqui, la ruta coincide
    return parametros


# procesa rutas y transiciones
def resolver_enrutamiento():
    
    # obtiene la ruta absoluta del archivo actual
    base_dir = os.path.dirname(os.path.abspath(__file__))

    # construye rutas completas de entrada y salida
    input_path = os.path.join(base_dir, "input.txt")
    output_path = os.path.join(base_dir, "output.txt")

    # lista donde se almacenan las respuestas finales
    resultados = []

    #lectura de datos iniciales desde archivo
    with open(input_path, "r", encoding="utf-8") as archivo:

        # redefinimos input para leer linea a linea del archivo
        input = archivo.readline

        # leer numero de rutas
        N = int(input().strip())

        # lista donde se guardan las rutas
        # cada elemento es (ruta, contenido)
        rutas = []

        # leer las "N" rutas
        for _ in range(N):
            linea = input().strip()

            # separar en ruta y contenido
            ruta, contenido = linea.split(maxsplit=1)

            rutas.append((ruta, contenido))

        # leer numero de transiciones "M"
        M = int(input().strip())

        # procesamiento
        for _ in range(M):

            # leer una transicion
            transicion = input().strip()

            # bandera para saber si se encontro coincidencia
            encontrado = False

            # recorrer todas las rutas registradas
            for ruta, contenido in rutas:
                # verificar si coincide
                parametros = coincide_ruta(ruta, transicion)
                # si coincide la ruta
                if parametros is not None:

                    # inicializamos la salida con el contenido base
                    salida = contenido

                    # reemplazar placeholders {id} por valores reales
                    for valor in parametros:
                        salida = salida.replace("{id}", valor, 1)

                    # si NO hay {id}, pero hay parametros,
                    # se agregan al final (caso general)
                    if "{id}" not in contenido and parametros:
                        salida += " " + " ".join(parametros)

                    # guardar resultado
                    resultados.append(salida)

                    encontrado = True
                    break  # ya encontramos la ruta correcta

            # si no se encontro ninguna coincidencia
            if not encontrado:
                resultados.append("404 Not Found")

    # escritura de resultado en un archivo
    with open(output_path, "w", encoding="utf-8") as salida:

        # escribir cada linea en el archivo
        for linea in resultados:
            salida.write(linea + "\n")

    # impresion de resultado por terminal
    for linea in resultados:
        print(linea)

#ejecucion del programa principal que consume "resolver_enrutamiento"
if __name__ == "__main__":
    resolver_enrutamiento()