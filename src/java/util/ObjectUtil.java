package util;

import graph.ClassNode;
import hirondelle.web4j.model.ModelUtil;

import java.util.List;
/*
 * Rubus: A Compiler for Seamless and Extensible Parallelism
 * 
 * Copyright (C) 2017 Muhammad Adnan - University of the Punjab
 * 
 * This file is part of Rubus.
 * Rubus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.

 * Rubus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Rubus. If not, see <http://www.gnu.org/licenses/>.
 */

public class ObjectUtil {

	/**
	 * Print object description
	 * @param object
	 */
	public static void println(Object object) {
		try {
			System.out.println(ModelUtil.toStringAvoidCyclicRefs(object,ClassNode.class, "toString"));
		} catch (Exception e) {
			// TODO: handle exception
		}
		
	}
	
	/**
	 * Print object description
	 * @param object
	 */
	public static void println(List objects) {
		try {
			for (Object obj:objects) {
				System.out.println(ModelUtil.toStringAvoidCyclicRefs(obj,ClassNode.class, "toString"));
				
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		
	}
	
	public static void println(String string) {
		System.out.println(string);
		

	
		
	}
	
	
	public static String toString(Object object) {
		return ModelUtil.toStringFor(object);
		
	}
	
//	/***
//	 * Convert an object in Json format if possible
//	 * @param object
//	 * @return
//	 */
//	
//	public static String toJSON(Class<?> object) {
//		Field[] fields = object.getClass().getFields();
//		ObjectMapper mapper = new ObjectMapper();
//		try {
//			HashMap<String,String> cacheMap = new HashMap<String, String>();
//
//
//			for (Field field:fields) {
//				field.setAccessible(true);
//				
////				  ParameterizedType integerListType = (ParameterizedType) field.getGenericType();
////			        Class<?> integerListClass = (Class<?>) integerListType.getActualTypeArguments()[0];
////				Log.d("Types: ", "getType() : "+field.getType()+ " generic type: " +integerListClass);
//				cacheMap.put(field.getName(),mapper.writeValueAsString(field.get(field.getType())));
//			}
//
//		
//			return mapper.writeValueAsString(cacheMap);
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//return null;
//	}
//	
//	public static String toJSON(Object object) {
//		Field[] fields = object.getClass().getDeclaredFields();
//		ObjectMapper mapper = new ObjectMapper();
//		try {
//			HashMap<String,String> cacheMap = new HashMap<String, String>();
//
//
//			for (Field field:fields) {
//				field.setAccessible(true);
//				
////				  ParameterizedType integerListType = (ParameterizedType) field.getGenericType();
////			        Class<?> integerListClass = (Class<?>) integerListType.getActualTypeArguments()[0];
////				Log.d("Types: ", "getType() : "+field.getType()+ " generic type: " +integerListClass);
//				cacheMap.put(field.getName(),mapper.writeValueAsString(field.get(field.getType())));
//			}
//
//			return mapper.writeValueAsString(cacheMap);
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//return null;
//	}
//	
//	
//	
//
//	public static void toObject(String json, Object object)  {
//
//		
//	
//			ObjectMapper mapper = new ObjectMapper();
//			HashMap<String,String> cacheMap = new HashMap<String, String>(); 
//			try {
//				cacheMap = mapper.readValue(json, cacheMap.getClass());
//			} catch (JsonParseException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			} catch (JsonMappingException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			} catch (IOException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			} 	
//			Field[] fields = object.getClass().getFields();
//
//			for (Field field:fields) {
//				field.setAccessible(true);
//				String value = cacheMap.get(field.getName());
//					try {
//						//  mapper.getTypeFactory().constructParametricType(Data.class, contentClass.class);
//						if(field.getType() == HashMap.class){
//							    ParameterizedType integerListType = (ParameterizedType) field.getGenericType();
//							    Class<?> classType = (Class<?>)field.getType().getClass();
//							    Class<?> keyType = (Class<?>) integerListType.getActualTypeArguments()[0];
//						        Class<?> valueType = (Class<?>) integerListType.getActualTypeArguments()[1];
//						    
//						        TypeFactory typeFactory = mapper.getTypeFactory();
//						        MapType mapType = typeFactory.constructMapType(HashMap.class, keyType, valueType);
//						        
//						        field.set(null,mapper.readValue(value,mapType));
//						}else if(field.getType() == ArrayList.class){
//							  ParameterizedType integerListType = (ParameterizedType) field.getGenericType();
//							   // Class<?> classType = (Class<?>)field.getType().getClass();
//							   // Class<?> keyType = (Class<?>) integerListType.getActualTypeArguments()[0];
//						        Class<?> valueType = (Class<?>) integerListType.getActualTypeArguments()[0];
//						    
//						        TypeFactory typeFactory = mapper.getTypeFactory();
//						        JavaType type = typeFactory.
//						                constructCollectionType(ArrayList.class, valueType) ;
//						        
//						        field.set(null,mapper.readValue(value,type));
//						}else {
//						  
//						  // Log.d("Types: ", "getType() : "+field.getType()+ " generic type: " +integerListClass);
//						
//						field.set(null,mapper.readValue(value,field.getType()));
//						}
//					} catch (JsonParseException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (JsonMappingException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (IllegalArgumentException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (IllegalAccessException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} // pass null as first parameter because it is static variable, pass object reference otherwise
//			}
//			
//			
//
//		}
	
	
}
