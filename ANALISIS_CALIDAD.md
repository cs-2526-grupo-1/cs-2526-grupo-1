# Práctica 2: Análisis de Calidad del Código (Bad Smells) - Grupo 1

## Integrantes del grupo 1
- Daniel Bonachela Martínez
- Marcelo Atanasio Domínguez Mateo
- Gonzalo Fernández de Córdoba García
- Alejandro García Prada
- Sara Guillén Martínez
- Samuel Melián Benito

## Captura de Pantalla del Overview de SonarQube

![Overview SonarQube 1](img/sonar-overview-1.png)
![Overview SonarQube 2](img/sonar-overview-2.png)

## Análisis de Calidad - Issues 

A continuación se muestra un resumen de los issues encontrados en el análisis de calidad realizado con SonarQube y mediante el análisis manual del código:

### Issue 1: Duplicación de Strings (Magic Strings) - Detectado por SonarQube

**Reporte de la issue**:
![Magic Strings](img/bad-smell-magic-strings)

**Ubicación de la issue**
Clase AccountService.java, en múltiples líneas (107, 114, 156, 163)
  
**Explicación de los alumnos del mal olor detectado** 
- El uso de Strings literales repetidos a lo largo del código, lo que usualmente se conoce como *Magic Strings*, hace que el sistema sea algo frágil. En caso de que se necesite modificar este mensaje literal que se repite en múltiples ocasiones (*"Deposit Confirmation"*), habrá que buscar en todo el código ese mensaje y reemplazarlo manualmente. Si olvidamos uno de ellos se pueden crear inconsistencias y comportamientos no esperados.

- Por qué **NO es un falso positivo (Issue real)**: No es un falso positivo puesto que esta repetición de cadenas en 4 ocasiones viola directamente el principio conocido como DRY (*Don't repeat yourself*). Al no tener estos Strings centralizados en variables o constantes, cualquier cambio requerirá modificar la lógica de servicio, lo que aumentará la probabilidad de bugs o inconsistencias como ya hemos comentado anteriormente.

**Refactorización**

NO REALIZAR HATA LA TAREA 3

Se utilizará una captura de pantalla del código o código resaltado para mostrar la solución. Se acompañará dicha solución de un breve comentario explicándola.

