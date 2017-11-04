package codeexport;
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


import java.io.File;
import java.util.ArrayList;

/*
 enum AccessModifire{
	 PUBLIC ("public"),
	 STATIC ("static")
	 ,	 PRIVATE ("private"),	 PROTECTED("protected"),	 NATIVE ("native")
			 ;
	 
	 private String value;
	 AccessModifire(String value){
		 this.value = value;
	 }
 }
 
 enum ReturnTypes{
	 VOID ("void"),
	
			 ;
	 
	 private String value;
	 ReturnTypes(String value){
		 this.value = value;
	 }
 }
*/

public class SClassFile implements SBlock{
  private File path;
  private String packageName;
  private String start;
  private String end;
  private String name;

public String getName() {
	return name;
}

public void setName(String name) {
	this.name = name;
}

public String getPackageName() {
	return packageName;
}

public void setPackageName(String packageName) {
	this.packageName = packageName;
}


public String getStart() {
	return start;
}

public void setStart(String start) {
	this.start = start;
}

public String getEnd() {
	return end;
}

public void setEnd(String end) {
	this.end = end;
}

public SClassFile(File path) {
	this.path = path;
}

@Override
public void accept(SClassFileVisitor visitor) {
	visitor.visit(this);
	
}

private ArrayList<SField> fields = new ArrayList<SField>();
private ArrayList<SMethod> methods = new ArrayList<SMethod>();
private String extention;

public void addMethod(SMethod method){
	this.methods.add(method);
}
public void removeMethod(SMethod method){
	this.methods.remove(method);
}
public void addField(SField field){
	this.fields.add(field);
}
public void addUniqueField(SField field){
	for (SField f : fields) {
    if(f.getFieldDecliration().equals(field.getFieldDecliration())) return;		
	}
	this.fields.add(field);
}

public void removeField(SField field){
	this.fields.add(field);
}
public File getPath() {
	return path;
}

public void setPath(File path) {
	this.path = path;
}

public ArrayList<SField> getFields() {
	return fields;
}

public void setFields(ArrayList<SField> fields) {
	this.fields = fields;
}

public ArrayList<SMethod> getMethods() {
	return methods;
}

public void setMethods(ArrayList<SMethod> method) {
	this.methods = method;
}

public void addUniqueMethod(SMethod method){
	if(!this.methods.contains(method))
	     this.methods.add(method);
}

public File getDestinationFile(){
	return new File(path,name+extention);
}

public void setExtention(String extention) {
	this.extention = extention;
	
}

}
