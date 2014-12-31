jsonstream
==========

A fast streaming parser for JSON.

Example:

```
String jsonString = "{\"key\": \"value\"}";
JsonReader jsonReader = JsonBuilder().createReader(jsonString);
Map<String, Object> result = jsonReader.parse();
```
