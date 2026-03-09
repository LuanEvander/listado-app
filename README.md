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

## Snapshot atual

- versão: `1.0.0-alpha`
- objetivo: validar se o produto atual atende às expectativas iniciais dos stakeholders;
- escopo deste snapshot: fluxos principais implementados, build validado e documentação consolidada;
- fora do escopo deste snapshot: suíte formal de testes automatizados e rodada estruturada de testes com usuários.

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
- o subtotal exibido no item reage imediatamente aos valores digitados, reduzindo inconsistências visuais;
- o rótulo intermediário de `preço base` foi removido do item da lista para simplificar a leitura.

### 2026-03-09 — edição de itens da lista passa a usar salvamento automático

Regra incorporada:

- a edição de quantidade e preço deixa de depender de ação manual explícita;
- após uma breve pausa na digitação, os valores são persistidos automaticamente;
- o subtotal do item continua reagindo instantaneamente durante a edição;
- a interface evita notificações de sucesso repetitivas durante autosave.

### 2026-03-09 — home e itens da lista recebem simplificações de navegação

Regra incorporada:

- a home deixa de exibir botões redundantes para áreas já acessíveis pela navegação principal;
- o checkbox do item da lista passa a ocupar a área de destaque do cabeçalho do card;
- tocar em toda a caixa do item expande ou recolhe seus detalhes.

### 2026-03-09 — listas abertas ganham exclusão e edição numérica menos intrusiva

Regra incorporada:

- cada lista aberta passa a oferecer exclusão rápida ao lado da ação de edição;
- a exclusão pede confirmação antes de remover a lista e seus itens vinculados;
- campos numéricos deixam de sobrescrever a digitação do usuário com sufixos como `4.0` durante o preenchimento.

### 2026-03-09 — detalhe da lista passa a destacar total e orçamento

Regra incorporada:

- o detalhe da lista passa a exibir total acumulado, total comprado e quantidade de itens marcados;
- quando houver orçamento, a tela informa saldo restante ou excesso atual durante o planejamento e a execução;
- as análises deixam de destacar preço normalizado na interface e passam a priorizar o preço informado na compra.

### 2026-03-09 — snapshot 1.0.0-alpha é consolidado para aceite inicial

Resultado incorporado:

- o projeto passa a ser tratado como snapshot `1.0.0-alpha`;
- a entrega fica posicionada como base para validação de aderência ao planejamento inicial;
- eventuais ajustes funcionais podem ocorrer antes da etapa `1.0.0-beta`, quando testes e validações de uso terão prioridade ampliada.

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
