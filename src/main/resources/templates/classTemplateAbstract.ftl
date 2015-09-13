<#-- Utilities Functions -->
<#-- Check id the column is the primary key #-->
<#function getPkConstraint changeSet table column >
	<#if (column.constraints.@primaryKey)?? && (column.constraints.@constraintName)??>
	<#-- seems like the constraints primaryKey is a sequence. Weird... -->
		<#list column.constraints.@primaryKey as pk> 
			<#if pk == "true">
				<#return " id">
			</#if>
		</#list>
	<#elseif (changeSet.addPrimaryKey)?? >
		<#list changeSet.addPrimaryKey as pk>
			<#if (pk.@tableName)?? && (pk.@columnNames)?? && pk.@tableName==table.@tableName && pk.@columnNames==column.@name>
				<#return " id">
			</#if>
		</#list>
	</#if>
		<#return "">
</#function>

<#-- Check the null constraint on a column #-->
<#function getNullConstraint column>
	<#if (column.constraints.@nullable)?? && column.constraints.@nullable == "false">
		<#return " not_null">
	<#else>
		<#return " null">
	</#if>
</#function>

<#-- Return the constraints on a column #-->
<#function getConstraints changeSet table column>
	<#return getNullConstraint(column) + getPkConstraint(changeSet table column)>
</#function>

<#-- Mapping between db types and Cpp types-->
<#-- TODO use propertie file to do that -->
<#function getJavaType dbType>
 	<#assign res = dbType?trim?upper_case?matches(r"([A-Z]*).*")>
	<#if res>
		<#return dbMapping[res?groups[1]]!dbType>
	<#else>
		<#return dbMapping[dbType?trim?upper_case]!dbType>
	</#if>
</#function>
<#-- Creation of the abstract persisted object -->
<#list model.changeSet.createTable as table>
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
}</#list>