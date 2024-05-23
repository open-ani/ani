## data-sources.api.test-generator

从数据源拉取数据, 使用当前 `PatternBasedRawTitleParser` 解析, 生成单元测试用例.
生成的用例需要人工审核以确保正确性.

这些测试用于未来更新 parser 时, 保证不会产生 regression.
