Source avro schema:
```json
{
  "type": "record",
  "name": "event",
  "namespace": "project",
  "fields": [
    {
      "name": "id",
      "type": "long"
    },
    {
      "name": "value",
      "type": "string"
    },
    {
      "name": "timestamp",
      "type": "string"
    }
  ]
}
```

Target avro schema:
```json
{
  "type": "record",
  "name": "eventInfo",
  "namespace": "project",
  "fields": [
    {
      "name": "id",
      "type": "long"
    },
    {
      "name": "count",
      "type": "long"
    },
    {
      "name": "startTime",
      "type": "string"
    },
    {
      "name": "endTime",
      "type": "string"
    }
  ]
}
```