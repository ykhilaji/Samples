#@namespace scala finagle.rpc

struct Request {
 1:required string expression
}

struct Response {
 1:required double result
}

service CalculatorService {
  Response task(1: Request x)
}