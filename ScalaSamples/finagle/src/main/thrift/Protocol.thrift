#@namespace scala finagleThriftSample

struct Request {
 1:required i64 id,
 2:required string body
}

struct Response {
 1:required string result
}

service RPCService {
  Response task(1: Request x)
  oneway void ping()
}