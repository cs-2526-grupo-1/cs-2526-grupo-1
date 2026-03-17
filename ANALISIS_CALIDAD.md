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

### Issue 8: Variables locales no utilizadas - Detectado por SonarQube

**Reporte de la issue**:

![Variables locales no utilizadas](img/bad-smell-unused-local-variable.png)

**Ubicación de la issue**

Clase `AccountService.java`, línea 185

**Explicación de los alumnos del mal olor detectado**
- Dentro del método `withdraw` hemos detectado la declaración de una variable llamada `seccondAccount` que no se utiliza a lo largo del código. Además, hay una falta de ortografía en la palabra seccond. No sabemos si es un residuo de una refactorización anterior o si el desarrollador tendría intención de implementar una segunda cuenta la cual al final no llevó a cabo. Esto ensucia el código y dificulta la lectura del método.

- Por qué **NO es un falso positivo (Issue real)**: Creemos que es un issue real porque este tipo de variables hacen que el código sea más difícil de entender. Cuando estás leyendo la función pierdes tiempo buscando dónde se usa esa variable para luego darte cuenta de que no se utiliza. Esto es una mala práctica de limpieza de código, por lo que si una variable no aporta nada al funcionamiento lo mejor es borrarla para que el método sea más sencillo de leer y mantener.

### Issue 9: Uso de tipos primitivos para amount - Detectado por análisis manual

**Reporte de la issue**:

![Uso double para amount](img/bad-smell-amount1.png)
![Uso double para amount](img/bad-smell-amount2.png)

**Ubicación de la issue**

Clase `AccountService.java`, líneas 77, 126, 175, 223, 314
  
**Explicación de los alumnos del mal olor detectado**
- Nos hemos dado cuenta de que para gestionar los saldos y las cantidades de las transferencias se está usando el tipo `double`. El problema es que los `double` no son exactos para temas de dinero porque funcionan con un sistema de coma flotante binaria, es decir, cuando se realizan operaciones matemáticas pueden aparecer decimales infinitos o errores de precisión muy raros. Por ejemplo, te puede pasar que una cuenta que debería tener $0.30$ acabe teniendo $0.30000000000000004$ por un error de redondeo, por lo que puede llegar a ser un problema bastante crítico. Es un problema real y bastante grave porque pone en peligro la fiabilidad de los datos financieros. Si usásemos BigDecimal o una clase propia llamada Money podríamos controlar exactamente cuántos decimales queremos y cómo queremos que se haga el redondeo. Al tenerlo como un double habría que gestionar los redondeos y el formato en cada método donde se haga el cálculo. Esto implica que la responsabilidad de cómo tratar el dinero acabe dispersa por todo el `AccountService` en lugar de estar en un solo sitio centralizado . Si esto se quedase así a la larga habrá desajustes en las cuentas de los clientes y será casi imposible encontrar dónde empezó el error.


