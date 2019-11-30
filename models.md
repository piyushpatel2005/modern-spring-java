# Data Models

In this app, we have following tables.

- **Ingredient** to hold ingredient information.
- **Taco** holds essential information about a taco design.
- **Taco_Ingredients** contains one or more rows for each row in Taco, mapping the taco to the ingredients for that taco.
- **Taco_Order** holds essential order details
- **Taco_Order_Tacos** contains one or more rows for each row in `Taco_Order` mapping the order to the tacos in the order.

If there's file named `schema.sql` in the root of application's classpath, then SQL in that file will be executed against the database when the application starts. Check that file in `src/main/resources` folder. Spring Boot will also execute a file named `data.sql` from the root of the classpath when application starts.