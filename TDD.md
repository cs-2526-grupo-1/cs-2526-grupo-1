# Tarea 4: Implementación de una funcionalidad con TDD 

Se proporciona un ejemplo de cómo reportar la implementación de una funcionalidad utilizando TDD. 

### Ejemplo 1

**INPUT y OUTPUT**: 1 -> "I"

**EJ1. Código de test**
```java
@Test
public void testI() {
    RomanConverter converter = new RomanConverter();
    assertEquals("I", converter.convert(1));
}
```

**EJ1. Mensaje del test añadido que NO PASA**

```log
org.opentest4j.AssertionFailedError: expected: [I] but was: []
```

**EJ1. Código mínimo para que el test pase**

Describe brevemente el código mínimo implementado

```java
public String convert(int number){
    return "I";
}
```

**EJ1. Captura de que TODOS los test PASAN**

![Pasa](img/capturas/Ejemplo_1_PASA.png "Pasa")

**EJ1. Refactorización**
> [BORRAR]  Solo si se considera necesario

Justificar vuestra refactorización aquí.

```java
public String convert(int number){
    return "I"; // Imaginemos que hemos refactorizado aquí
}
```
**EJ1. Captura de que TODOS los tests PASAN tras la refactorización**
> [BORRAR]  Solo si se ha realizado una refactorización

![Pasa](img/capturas/Ejemplo_1_PASA.png "Pasa")