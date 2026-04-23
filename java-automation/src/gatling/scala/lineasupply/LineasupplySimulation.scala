package lineasupply

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._

class LineasupplySimulation extends Simulation {

  val httpProtocol = http.baseUrl("http://localhost:8001")

  val browse = scenario("Browse products")
    .exec(http("GET /products").get("/products").check(status.is(200)))
    .pause(1)
    .exec(http("GET /products/{id}").get("/products/1").check(status.is(200)))
    .pause(1)
    .exec(http("Search").get("/products?search=shirt").check(status.is(200)))

  setUp(
    browse.inject(rampUsers(25).during(30.seconds))
  ).protocols(httpProtocol)
    .assertions(
      global.responseTime.percentile(95).lt(500),
      global.failedRequests.percent.lt(2)
    )
}
