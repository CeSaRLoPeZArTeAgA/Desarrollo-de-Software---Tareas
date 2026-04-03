from collections import defaultdict
import os

def main():
    #ruta actual 
    base_dir = os.path.dirname(os.path.abspath(__file__))

    #ruta del archivo de entrada
    input_path = os.path.join(base_dir, "input.txt")

    #ruta del archivo de salida
    output_path = os.path.join(base_dir, "output.txt")

    #apertura de archivo de entrada
    with open(input_path, "r", encoding="utf-8") as archivo:

        # se redefine input para leer linea a linea del archivo
        input = archivo.readline

        
        # VALIDAR PRIMERA LINEA
        # lectura del archivo y limpia de la cadena(elimina espacios y cadenas de terminacion final)
        linea = input().strip()

        #si la linea esta vacia, se detiene el programa
        if not linea:
            raise ValueError("Error: la primera línea está vacía")

        partes = linea.split()

        # Deben ser exactamente 3 valores
        if len(partes) != 3:
            raise ValueError("Error: la primera línea debe contener exactamente 3 enteros")

        try:
            N, M, S = map(int, partes)
        except:
            raise ValueError("Error: los valores deben ser enteros")

        # Validación de rango
        if not (1 <= N <= 1000):
            raise ValueError(f"Error: N fuera de rango (N={N})")

        if not (1 <= M <= 1000):
            raise ValueError(f"Error: M fuera de rango (M={M})")

        if not (1 <= S <= 1000):
            raise ValueError(f"Error: S fuera de rango (S={S})")



        # lectura de parametros iniciales, en la primera linea del archivo
        # N - numero de socios
        # M - numero de terminales
        # S - numero de transacciones
        N, M, S = map(int, input().split())

        # mapeo terminal por socio, se usara diccionario
        # clave: terminal(t)
        # valor: socio(p)
        terminal_to_socio = {}

        # Leer las M líneas de terminales
        for _ in range(M):
            p, t = map(int, input().split())

            # Guardamos la relación
            terminal_to_socio[t] = p


        # definicion de la estructura de conteo
        # conteo[p][c] = número de compras del cliente c en el socio p
        #
        # defaultdict(int) inicializa automáticamente en 0
        conteo = {i: defaultdict(int) for i in range(1, N + 1)}
   
        #procesamiento de transacciones
        # se lee S transacciones (el S ya esta validado en la primera linea)
        for _ in range(S):
            c, t = map(int, input().split())

            #verificar si el terminal pertenece a algun socio
            if t not in terminal_to_socio:
                continue  # ignorar si no existe

            #obtener el socio correspondiente
            socio = terminal_to_socio[t]

            #incrementar el contador
            conteo[socio][c] += 1
  
        #determinacion de cliente mas fiel por socio
        resultado = [] #inicializamos 
        for socio in range(1, N + 1):
            clientes = conteo[socio]

            # caso: no hay transacciones
            if not clientes:
                resultado.append(f"{socio} -1")
                continue

            #variables auxiliares para inicializacion
            mejor_cliente = -1
            max_compras = -1

            #busqueda del maximo
            for c, compras in clientes.items():
                # 1. Mayor numero de compras
                # 2. Si hay empate, usar menor ID
                if (compras > max_compras or
                   (compras == max_compras and c < mejor_cliente)):
                    
                    max_compras = compras
                    mejor_cliente = c

            resultado.append(f"{socio} {mejor_cliente}")


    #guarda resultado de busqueda en archivo
    with open(output_path, "w", encoding="utf-8") as salida:
        for linea in resultado:
            salida.write(linea + "\n")

    #impresion de resultado por consola
    for linea in resultado:
        print(linea)

#consumo de la funcion main
if __name__ == "__main__":
    main()