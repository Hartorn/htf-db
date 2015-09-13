<#list model.changeSet.createTable as table>
/* This file can be used to add display methods, or test, or anything to an object */
#pragma db object table("${table.@tableName}")
class ${table.@tableName} final : public Abstract${table.@tableName}
{


}</#list>