#@namespace scala thriftSample

typedef i64 long

struct Message {
 1:required long id,
 2:required string body,
 3:optional string extra
}