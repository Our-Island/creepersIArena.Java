function choosejob:choose/choose {job_index:"1"}
advancement revoke @s only choosejob:job/1_trigger
#这里是因为原本用右键检测插件直接/function choosejob:choose {index:"1"}
#但是弃用了插件，可惜进度中reward的function只能指向路径，不能带参数，所以被迫用了这种方法