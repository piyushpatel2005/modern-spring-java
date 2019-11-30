# Spring Data JPA

Spring Data provides persistence against RDBMS using Spring Data JPA. There are also other projects for different databases like Spring Data MongoDB, Spring Data Neo4j, Spring Data Redis, Spring Data Cassandra. 

To add JPA to Spring Boot Application, we only need to add dependency for spring-boot-strater-data-jpa. By default, it will include Hibernate as JPA library. If we need different JPA implementation, we can use following code snippet to exclude Hibernate and include eclipselink.

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-jpa</artifactId>
  <exclusions>
    <exclusion>
      <artifactId>hibernate-entitymanager</artifactId>
      <groupId>org.hibernate</groupId>
    </exclusion>
  </exclusions>
</dependency>
<dependency>
  <groupId>org.eclipse.persistence</groupId>
  <artifactId>eclipselink</artifactId>
  <version>2.5.2</version>
</dependency>
```

With JPA implementation, we ca annotate each domain objects with `@Entity` and primary ID with `@Id` annotation. Also, each entity need no argument constructor. `@NoArgsConstructor` with `force=true` will set the attributes to null.

In `Taco` domain class we annotate `ingredients` with `@ManyToMany` to indicate many to many relationship. `@PrePersist` annotation is used to set the `createdAt` property to the current date and time before `Taco` is persisted.

With Spring Data, we can extend the `CrudRepository` interface and it will create methods automatically. It looks like

```java
public interface IngredientRepository
         extends CrudRepository<Ingredient, String> {

}
```

This CrudRepository takes two parameters. The first is the entity type the repository is to persist and the second one is the type of entity ID property. We do not need to implement any method, Spring Data JPA automatically generates on the fly. These need to be injected into controllers. If we want to create new methods like

`List<Order> findByDeliveryZip(String deliveryZip);`
Spring Data looks at method name and understands method purpose. It uses DSL (domain specific language). This method will know that it will have to find Order objects as it implements `CrudRepository` with `Order` as first parameter. When method starts by find, read, it knows that it is read opeartion. It will query by a property in the method name which comes after By keyword. We can also create method like `List<Order> readOrdersByDeliveryZipAndPlacedAtBetween(String deliveryZip, Date startDate, Date endDate);`. We can also use `OrderBy` at the end of method name to sort the results by a specific column like `List<Order> findByDeliveryCityOrderByDeliveryTo(String deliveryCity)`. WE can also create complicated method using `@Query` annotation and query specific object.

```java
@Query("Order o where o.deliveryCity='Seattle'")
List<Order> readOrdersDeliveredInSeattle();
```

Spring Data method signatures can include operators like IsAfter, After, IsGreaterThan, GreaterThan, IsGreaterThanEqual, IsBefore, Before, IsLessThanEqual, LessThanEqual,IsBetween, Between, IsNull, IsNotNull, NotNull, IsIn, IsTrue, IsNot, Not, IgnoringCase, IgnoresCase, AllIgnoringCase, AllIgnoresCase, etc.
