jsonstream
==========

A fast streaming parser for JSON.

Example:

```
String jsonString = "{\"key\": \"value\"}";
JsonStream jsonStream = JsonStreamBuilder(jsonString).createJsonStream();
Map<String, Object> result = jsonStream.parse(jsonString);
```

