namespace java test

typedef i32 int

enum Operation {
    ADD = 1,
    SUBTRACT = 2,
    MULTIPLY = 3,
    DIVIDE = 4
}

struct Message {
    1:int a,
    2:int b,
    3:Operation op,
    4:optional string comment
}

exception RPCError {
    1:int code
}

service RPCService {
    void ping()
    oneway void registerClient(1:int id)
    int nextRandom()
    int calculate(1:Message msg) throws (1:RPCError e)
}