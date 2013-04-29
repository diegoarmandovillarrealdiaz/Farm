Farm
====

El proyecto final se encuentra ubicado en :

https://github.com/diegoarmandovillarrealdiaz/Farm/tree/master/Proyecto_Final/Farm-Diego-Leidy

Supuestos :
====

- Todos los termómetros de una misma zona perciben al misma temperatura.
- Al cambiar de zona un ventilador o calentador, hay que reducir su nivel de energía a 0 (Desconectarlo).
- Al remover un termómetro de una zona hay que reducir a 0, el nivel de energía los ventiladores y calentadores.
- Al cambiar de zona un termómetro, se debe reducir el nivel de energía a 0 tanto de ventiladores como de calentadores ubicados en la zona donde se encontraba el termómetro, claro en caso de no haber más termometros (no se tiene punto de referencia).

Algoritmo usado:
====

El espíritu de esta rutina es hacer uso de los hilos y listeners. Por esta razón se cuenta con un hilo que monitorea la casa cada 300 milisegundos con el objetivo de tomar acciones. A su vez se han añadido unos listener a los dispositivos para detectar los cambios de zona (ver supuestos)  

1- Se obtiene todas las zonas que tiene por lo menos un termómetro

2- Se itera sobre cada una de las zonas devueltas:

2-1: Se lee la temperatura de uno de los termómetros ubicados en la zona  particular, con el objetivo de determinar  su temperatura

2-2: De acuerdo a la temperatura se realizan las siguientes acciones:

2-2-1: Si la temperatura es mayor al valor máximo establecido (300), se encienden los ventiladores y se apagan los calentadores.

2-2-2: Si la temperatura es menor que el valor mínimo establecido (290), se encienden los calentadores y se apagan los ventiladores.

2-2-3: en cualquier otro caso se apagan ventiladores y calentadores

3- Se duerme la ejecución actual por 300 milisegundos


Limitaciones de la rutina
====

- Realizar una revisión de la casa cada 300 milisegundos genera una sobre cargar computacional innecesario, cuando no hay cambios. 

Problemas de la implementación actual
====
- En el hijo se corre el riesgo de obtener una excepción a la hora de obtener la temperatura (Por algún motivo se mueve el único termómetro de una zona en el momento justo) , la siguiente linea puede fallar
  
  double temperatur = (getZonesthatcontainThermometers().get(zone).get(0)).getTemperature();


Posibles mejoras
====

- Modificar los métodos  co.edu.uis.sistemas.simple.icasa.SimpleIcasaComponent.setPowerLevelToAllCoolers(String, double) y co.edu.uis.sistemas.simple.icasa.SimpleIcasaComponent.setPowerLevelToAllHeaters(String, double) para que de acuerdo al número de ventiladores regule el nivel de poder de cada uno.
La idea sería definir un nivel de poder máximo por cuarto, para de esta forma ahorrar energía al no usar más de esa cantidad.

- Teniendo en cuenta lo anterior hay que definir un nivel mínimo de poder en cada ventilador y calentador, es decir por debajo de este número se usaría 0. Mantener niveles cercanos a cero no tiene mucho sentido. Dado que no sería perceptible un cambio en el temperatura del  cuarto.
