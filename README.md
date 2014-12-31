jsonstream
==========

A fast streaming parser for JSON.

Example:

```
String jsonString = "{\"key\": \"value\"}";
JsonReader jsonReader = JsonReaderBuilder(jsonString).create();
Map<String, Object> result = jsonReader.parse();
```
