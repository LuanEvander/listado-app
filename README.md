# Listado

App Android em Kotlin, Jetpack Compose e Gradle para gestão offline de listas de compras, catálogo de itens com dimensão fixa opcional e análises históricas de preço.

## Stack

- Kotlin
- Jetpack Compose + Material 3
- Navigation Compose
- Room
- Hilt
- Gradle Kotlin DSL

## Requisitos cobertos

- Cadastro, edição e inativação lógica de itens com dimensão fixa opcional
- Criação e edição de listas em andamento
- Cálculo automático de subtotal por item e total por lista
- Controle de orçamento por lista
- Bloqueio de edição após conclusão
- Histórico de listas concluídas
- Histórico de preços por item com normalização pelo conteúdo base de cada unidade comprada
- Pesquisa de itens para inclusão em listas

## Rastreabilidade de mudanças do produto

### 2026-03-08 — inclusão do atributo dimensão no item

Regra incorporada:

- cada item do catálogo passa a ter `nome`, `dimensão` e `unidade de medida`;
- a dimensão representa o conteúdo de uma unidade do item;
- na compra, a quantidade informada na lista representa o número de unidades/embalagens levadas;
- o preço normalizado passa a considerar `preço por unidade ÷ conteúdo da unidade`;
- variações como “Refri 600 ml” e “Refri 2 l” podem coexistir como itens distintos no catálogo.

### 2026-03-09 — categorias passam a ser definidas pelo sistema

Regra incorporada:

- categorias do catálogo deixaram de ser texto livre;
- o usuário agora escolhe a categoria a partir de uma lista fixa do sistema;
- a mudança reduz inconsistências de cadastro e melhora a padronização da busca e da análise histórica;
- a linha do tempo do produto passa a registrar a restrição como decisão permanente de domínio.

### 2026-03-09 — itens podem ser vendidos por medida variável

Regra incorporada:

- itens do catálogo agora podem ser cadastrados em dois modos: `com dimensão fixa` ou `sem dimensão fixa`;
- itens com dimensão fixa continuam usando `preço por unidade × quantidade`;
- itens sem dimensão fixa passam a usar `preço por medida × quantidade comprada`;
- exemplos como batata, tomate e carnes podem ser lançados com peso real, como `2,15 kg`;
- a análise histórica continua normalizada pela unidade base da medida.

### 2026-03-09 — itens da lista passam a ter visualização colapsável

Regra incorporada:

- itens de listas extensas podem ser expandidos ou recolhidos para melhorar a navegação;
- a atualização de `quantidade × preço` passa a ocorrer em uma única operação;
- o subtotal e o preço base exibidos no item reagem imediatamente aos valores digitados, reduzindo inconsistências visuais.

## Como executar

1. Instale Android Studio e JDK 17.
2. Garanta o Android SDK com a plataforma 36.
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
