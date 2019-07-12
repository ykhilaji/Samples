## Avro
# Generate classes
`mvn generate-sources`
# Add sources:
`mvn compile`

```xml
        <sourceDirectory>
            ${project.build.directory}/generated-sources/annotations/
        </sourceDirectory>
```