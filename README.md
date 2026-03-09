# Listado

App Android em Kotlin, Jetpack Compose e Gradle para gestão offline de listas de compras, catálogo de itens e análises históricas de preço.

## Stack

- Kotlin
- Jetpack Compose + Material 3
- Navigation Compose
- Room
- Hilt
- Gradle Kotlin DSL

## Requisitos cobertos

- Cadastro, edição e inativação lógica de itens
- Criação e edição de listas em andamento
- Cálculo automático de subtotal por item e total por lista
- Controle de orçamento por lista
- Bloqueio de edição após conclusão
- Histórico de listas concluídas
- Histórico de preços por item com normalização por unidade base
- Pesquisa de itens para inclusão em listas

## Como executar

1. Instale Android Studio e JDK 17.
2. Garanta o Android SDK com a plataforma 34.
3. Na raiz do projeto, gere/atualize o wrapper e execute:
   - `./gradlew assembleDebug`
4. Abra a pasta no Android Studio e rode no emulador ou dispositivo Android 10+.

## Estrutura

- `app/src/main/java/br/com/listado/core`: modelos e utilitários
- `app/src/main/java/br/com/listado/data`: Room, DAOs e repositórios
- `app/src/main/java/br/com/listado/feature`: telas e `ViewModel`s por feature
- `app/src/main/java/br/com/listado/ui`: navegação e tema
- `docs/analise-tecnica.md`: análise da documentação e decisões arquiteturais

## Convenções adotadas

- UDF na UI
- Estado persistido em banco local via Room
- `ViewModel` como state holder
- Compose com componentes pequenos e sem efeitos colaterais na renderização
- Nomenclatura e formatação alinhadas às convenções oficiais de Kotlin
- Commits em Conventional Commits, em PT-BR
