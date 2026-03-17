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
![Magic Strings](img/bad-smell-magic-strings.png)

**Ubicación de la issue**

Clase `AccountService.java`, en múltiples líneas (107, 114, 156, 163)
  
**Explicación de los alumnos del mal olor detectado** 
- El uso de Strings literales repetidos a lo largo del código, lo que usualmente se conoce como *Magic Strings*, hace que el sistema sea algo frágil. En caso de que se necesite modificar este mensaje literal que se repite en múltiples ocasiones (*"Deposit Confirmation"*), habrá que buscar en todo el código ese mensaje y reemplazarlo manualmente. Si olvidamos uno de ellos se pueden crear inconsistencias y comportamientos no esperados.

- Por qué **NO es un falso positivo (Issue real)**: No es un falso positivo puesto que esta repetición de cadenas en 4 ocasiones viola directamente el principio conocido como DRY (*Don't repeat yourself*). Al no tener estos Strings centralizados en variables o constantes, cualquier cambio requerirá modificar la lógica de servicio, lo que aumentará la probabilidad de bugs o inconsistencias como ya hemos comentado anteriormente.

### Issue 2: Nombres de variables y métodos poco descriptivos - Detectado por análisis manual

**Reporte de la issue**:
![Magic Strings](img/bad-smell-var-method-names-1.png)
![Magic Strings](img/bad-smell-var-method-names-2.png)

**Ubicación de la issue**

Clase `AccountService.java`, líneas 231, 232, 301
  
**Explicación de los alumnos del mal olor detectado**
- Hay dos variables de tipo `Account` llamadas `m` y `o`, y en el código no se aporta ningún contexto sobre qué representan (parecen ser cuenta de origen y cuenta de destino, pero lo desconocemos). Obligan a quien lee el código a deducir su propósito leyendo el resto de la función `transfer`. De igual manera, se nombra como `rm` al método para eliminar una cuenta en lugar de darle otro nombre más adecuado como deleteAccount o removeAccount. Estos nombres tan poco descriptivos obligan a estar constantemente "traduciendo" e interpretando el código, lo que dificulta detectar errores lógicos e impacta de forma negativa en la mantenibilidad.

### Issue 7: Large Class - Detectado por análisis manual

**Reporte de la issue**:
![God Class](img/bad-smell-god-class.png)

**Ubicación de la issue**

Clase `AccountService.java` (al completo)

**Explicación de los alumnos del mal olor detectado**
- Una *Large Class* es una clase que aglutina numerosas responsabilidades sin las que el programa funcionaría. En nuestro caso, `AccountService` cuenta con la gestión de las cuentas, las validaciones y las operaciones del negocio, lo cual entra perfectamente en la definición.

- Se evidencia en el constructor, debido a la gran cantidad de funcionalidades es muy grande y hereda multitud de objetos.

- Esta acumulación de responsabilidades induce una violación del **Principio de Responsabilidad Única (SRP)**, ya que por razones ya apuntadas son muchas las funciones de la clase. Esto a la larga acabará dificultando el mantenimiento y aumentando el riesgo de errores. Además, aumenta sensiblemente el acoplamiento del código, lo cual, es algo a evitar en cualquier programa orientado a objetos.


### Issue 8: Comentarios poco útiles o mal estructurados - Detectado por análisis manual

**Reporte de la issue**:
![Comentarios](img/bad-smell-comments.png)

**Ubicación de la issue**

Clase `AccountService.java`, en la cabecera métodos

**Explicación de los alumnos del mal olor detectado**
- A lo largo del código se puede ver que alguien se esforzó por dejar constancia de que hacía el código, pero este no sigue ningún estándar. Además, algunos ni siquiera aportan información, simplemente describen superficialmente aquello que ya se puede inferir leyendo superficialmente el código.
- Los comentarios superficiales no aportan valor al código y pueden inducir a error. Si el código cambia y los comentarios no se actualizan, la información que contienen deja de ser fiable. Esto afecta a la mantenibilidad y dificulta que otros desarrolladores comprendan el código.