### 方法一:
```
ObjectMapper mapper = new ObjectMapper();  
JavaType javaType = mapper.getTypeFactory().constructParametricType(List.class, Bean.class);  
//如果是Map类型  mapper.getTypeFactory().constructParametricType(HashMap.class,String.class, Bean.class);  
List<Bean> lst =  (List<Bean>)mapper.readValue(jsonString, javaType);   
```

### 方法二: 
```
ObjectMapper mapper = new ObjectMapper();  
List<Bean> beanList = mapper.readValue(jsonString, new TypeReference<List<Bean>>() {});   
```