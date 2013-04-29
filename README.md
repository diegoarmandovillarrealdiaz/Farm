Farm
====

El proyecto final se encuentra ubicado en :

https://github.com/diegoarmandovillarrealdiaz/Farm/tree/master/Proyecto_Final/Farm-Diego-Leidy

Supuestos :
====

- Todos los termómetros de una misma zona perciben al misma temperatura.
- Al cambiar de zona un ventilador o calentador, hay que reducir su nivel de energía a 0 (Desconectarlo).
- Al remover un termómetro de una zona hay que reducir a 0, el nivel de energía los ventiladores y calentadores.

Algoritmo usado:
====

El espíritu de esta rutina es hacer uso de los hilos y listeners. Por esta razón se cuenta con un hilo que monitorea la casa cada 300 milisegundos con el objetivo de tomar acciones. A su vez se han añadido unos listener a los dispositivos para detectar los cambios de zona (ver supuestos)  
1- Se obtiene todas las zonas que tiene por lo menos un termómetro
2- Se itera sobre cada una de las zonas devueltas:
  2-1: Se lee la temperatura de uno de los termómetros ubicados en la zona  particular, con el objetivo de determinar  su temperatura
  2-2: De acuerdo a la temperatura se realizan las siguientes acciones:
    2-2-1: Si la temperatura es mayor al valor máximo establecido, se encienden los ventiladores y se apagan los calentadores.
    2-2-2: Si la temperatura es menor que el valor mínimo establecido, se encienden los calentadores y se apagan los ventiladores.
    2-2-3: en cualquier otro caso se apagan ventiladores y calentadores
3- Se duerme la ejecución actual por 300 milisegundos

Otros
______
