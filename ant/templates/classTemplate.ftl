<#include "/utilities.ftl">
<#-- Creation of the abstract persisted object -->
<@pp.dropOutputFile />
<#list model.changeSet.createTable as table>
<@pp.changeOutputFile name="/abstract/Abstract"+table.@tableName+".hpp" />
#include <odb/core.hpp> 
/* This file is generated with each model change, do not modify, do not add or remove code here */
/* This class and its parent ${table.@tableName} are going to be used to persist object in database */
#pragma db object abstract table("${table.@tableName}")
class Abstract${table.@tableName}
{
	public:
	<#list table.column as column>
		void set${column.@name}(${getJavaType(column.@type)});
		${getJavaType(column.@type)} get${column.@name}();
	</#list>

	private:
	<#list table.column as column>
		#pragma db member column("${column.@name?upper_case}")${getConstraints(model.changeSet table column)}
		${getJavaType(column.@type)} ${column.@name}_;
	</#list>    
};

<#-- Creation of the persisted object (inheriting the abstract one) -->
<@pp.changeOutputFile name="/impl/"+table.@tableName+".hpp" />
/* This file can be used to add display methods, or test, or anything to an object */
#pragma db object table("${table.@tableName}")
class ${table.@tableName} final : public Abstract${table.@tableName}
{

<#assign keys = dbMapping?keys>
<#list keys as key>${key}=${dbMapping[key]};</#list>  
 
<#assign values = dbMapping?values>
<#list values as value>${value}; </#list>  
}</#list>