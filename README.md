# ecommanrce app

Minimal Spring Boot Maven project.

Run locally:

```bash
cd Assessment/springboot-app
mvn package
java -jar target/ecommanrce-app-0.0.1-SNAPSHOT.jar
```

Then open http://localhost:8080/ to see the greeting.

API Endpoints (basic):

- `POST /api/auth/register` {username,email,password}
- `POST /api/auth/login` {username,password} -> returns `token`
- `GET /api/products`
- `GET /api/products/{id}`
- `POST /api/products` (admin)
- `PUT /api/products/{id}` (admin)
- `DELETE /api/products/{id}` (admin)
- `GET /api/cart` (authenticated)
- `POST /api/cart/add?productId={id}&qty={n}` (authenticated)
- `POST /api/cart/remove?cartItemId={id}` (authenticated)
- `POST /api/orders/create?shippingAddress=...` (authenticated) -> returns orderId and paymentUrl

Run locally:

```bash
cd Assessment/springboot-app
# build (requires Maven installed)
mvn clean package

# run
java -jar target/ecommanrce-app-0.0.1-SNAPSHOT.jar
```

Notes:
- Configure `jwt.secret` and `stripe.api.key` in `src/main/resources/application.properties` or as environment variables before running.
- H2 console available at `/h2-console` in dev.
