cd src/
javac io/github/xmchxup/JackTokenizer.java io/github/xmchxup/JackAnalyzer.java io/github/xmchxup/CompilationEngine.java
java io.github.xmchxup.JackAnalyzer [directory, filename]

> 使用了格式化XML了 在这里输出有问题例
`<a></a>`会被合并成`<a/>`

目前解决方案: 不格式化或者格式化生成html，这里采用第二种，并且人肉排除下错误。成功了！😂
```bash
$ sh test.sh
10,11c10
<     <parameterList>
<     </parameterList>
---
>     <parameterList></parameterList>
272,273c271
<           <expressionList>
<           </expressionList>
---
>           <expressionList></expressionList>
16,17c16
<     <parameterList>
<     </parameterList>
---
>     <parameterList></parameterList>

…………
```

