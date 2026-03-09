# Análise técnica do projeto Listado

## 1. Leitura da documentação fornecida

### 1.1 Escopo identificado

Com base em `docs/relatório-final.pdf`, `docs/listado.asta` e nos diagramas exportados, o app foi concebido para:

- gerenciar catálogo reutilizável de itens;
- registrar a dimensão de cada item por unidade/embalagem;
- montar listas de compras com orçamento opcional;
- persistir tudo localmente;
- manter listas concluídas como histórico imutável;
- permitir análise temporal de preços por item;
- normalizar preços por unidade de medida com fator de conversão.

### 1.1.1 Mudança registrada em 2026-03-08

Após a primeira entrega, a regra de negócio do cadastro de itens foi refinada para incluir o atributo `dimensão`.

Nova interpretação do produto:

- o item cadastrado representa uma unidade comercial do produto;
- essa unidade possui um conteúdo próprio, expresso por `dimensão + unidade de medida`;
- a quantidade informada durante a compra representa quantas unidades/embalagens foram levadas;
- a análise de preço deve comparar o valor pago por unidade comercial e também o preço normalizado pelo conteúdo base.

Exemplo rastreado:

- `Refri`, `2`, `litros` → cada unidade do item contém 2 litros;
- ao comprar `3` unidades, o usuário levou 3 garrafas de 2 litros, e não 3 litros totais.

### 1.1.2 Mudança registrada em 2026-03-09

O catálogo deixou de aceitar categorias livres informadas pelo usuário.

Nova interpretação do produto:

- as categorias são limitadas e definidas pelo sistema;
- o cadastro de item passa a selecionar uma categoria pré-definida;
- a padronização evita duplicidades semânticas como `Bebida`, `Bebidas` e `Refrigerantes` para o mesmo grupo de produtos;
- a linha do tempo do produto deve registrar essa restrição como regra permanente do catálogo.

### 1.1.3 Mudança registrada em 2026-03-09

O catálogo passou a aceitar dois comportamentos de compra para o mesmo tipo de item.

Nova interpretação do produto:

- itens com dimensão fixa representam uma unidade comercial fechada, como `refrigerante 2 l`;
- itens sem dimensão fixa representam produtos vendidos por peso, volume ou contagem variável, como `batata`, `tomate` ou `carne`;
- para itens com dimensão fixa, o subtotal continua sendo `preço por unidade × quantidade de unidades`;
- para itens sem dimensão fixa, o subtotal passa a ser `preço por medida × quantidade comprada`;
- a análise histórica segue comparando preços normalizados na unidade base da medida.

### 1.1.4 Mudança registrada em 2026-03-09

O detalhamento da lista passou a priorizar legibilidade e consistência de atualização.

Nova interpretação do produto:

- itens da lista podem ser recolhidos ou expandidos sob demanda;
- listas extensas passam a ter navegação mais limpa, com foco nos resumos por item;
- a atualização de `quantidade` e `preço` deve ocorrer de forma atômica para evitar estados intermediários inconsistentes;
- o subtotal deve responder imediatamente aos valores digitados no formulário do item;
- o rótulo intermediário de `preço base` foi removido do card do item para reduzir ambiguidade.

### 1.1.5 Mudança registrada em 2026-03-09

O formulário de edição do item na lista passou a operar com salvamento automático.

Nova interpretação do produto:

- o usuário não precisa mais confirmar manualmente cada alteração de quantidade e preço;
- após uma pequena pausa na digitação, o item é persistido automaticamente;
- o feedback visual do subtotal continua imediato, enquanto a persistência ocorre em segundo plano;
- mensagens de sucesso contínuas deixam de ser exibidas para evitar ruído durante a edição.

### 1.1.6 Mudança registrada em 2026-03-09

Home e cards da lista receberam ajustes de navegação e interação.

Nova interpretação do produto:

- a home não precisa repetir atalhos para áreas já acessíveis pela navegação principal;
- o estado de compra do item passa a ficar evidente no topo do card por meio do checkbox;
- a expansão do item deve acontecer ao tocar em toda a caixa, e não apenas em um controle pontual.

### 1.1.7 Mudança registrada em 2026-03-09

Listas abertas e formulários numéricos receberam ajustes para reduzir fricção de uso.

Nova interpretação do produto:

- a listagem de listas abertas passa a expor a ação de exclusão ao lado da edição;
- a exclusão deve exigir confirmação explícita antes de remover a lista e seus itens associados;
- campos numéricos não devem reformatar o texto enquanto o usuário ainda está digitando, para não interromper o fluxo de entrada decimal.

### 1.1.8 Mudança registrada em 2026-03-09

O detalhamento da lista e a leitura das análises foram refinados para priorizar acompanhamento prático da compra.

Nova interpretação do produto:

- o cabeçalho da lista deve exibir total acumulado, total comprado e consumo do orçamento em tempo real;
- o saldo do orçamento deve considerar o total planejado antes da execução e o total já comprado durante a compra;
- a interface de análises deixa de enfatizar preço normalizado e passa a destacar o preço informado em cada compra.

### 1.2 Regras de negócio materializadas

- Itens inativados não aparecem em novas listas, mas seguem íntegros no histórico.
- Categorias de item são pré-definidas pelo sistema e não podem ser gerenciadas pelo usuário.
- Itens do catálogo podem ter dimensão fixa por unidade comercial ou serem vendidos por medida variável.
- A ocorrência de um item na lista é contextual e guarda “snapshot” próprio.
- A quantidade lançada na lista representa unidades compradas ou quantidade medida, conforme o modo do item.
- O preço base é calculado dividindo o preço informado pelo conteúdo base de referência de cada modo de compra.
- A edição de quantidade e preço no item da lista deve ser aplicada como uma única atualização lógica.
- A edição expandida do item na lista deve persistir automaticamente após pausa curta de digitação.
- A expansão e o recolhimento do item devem ocorrer ao tocar no card inteiro.
- A entrada de números deve preservar o texto digitado até o término natural da edição.
- A lista deve informar total acumulado e posição em relação ao orçamento durante o acompanhamento da compra.
- Uma lista concluída não pode mais ser alterada.
- A finalização exige ao menos um item marcado como comprado.
- Itens pendentes podem ser mantidos ou removidos no fechamento.
- A análise usa apenas itens efetivamente comprados.

### 1.3 Requisitos funcionais traduzidos para a implementação

| Requisito | Implementação |
| --- | --- |
| RF1-RF2 | Catálogo com cadastro, edição, inativação lógica, categorias fixas e dimensão opcional |
| RF3 | Unidades `g`, `kg`, `ml`, `l` e `un` com normalização para itens dimensionados ou vendidos por medida |
| RF4-RF6 | Criação, edição e detalhamento de listas abertas |
| RF7-RF8 | Finalização com bloqueio de edição e histórico |
| RF9-RF10 | Tela de análises com série histórica e métricas |
| RF11 | Busca de itens no catálogo e na inclusão em listas |

### 1.3.1 Impacto da mudança nos casos de uso

- o cadastro do item passou a exigir o preenchimento da dimensão;
- a inclusão do item na lista preserva um snapshot de `dimensão + unidade de medida`;
- a edição do item na lista passa a alterar `quantidade` e `preço` de acordo com o modo de compra do item;
- a escolha de unidade durante a compra deixou de ser um ajuste contextual, pois agora faz parte da definição do item do catálogo.
- a categoria do item deixou de ser texto livre e passou a ser escolhida a partir de uma lista fixa do sistema.
- itens fracionáveis passaram a aceitar quantidade decimal diretamente na medida de compra.
- os itens da lista passaram a oferecer visualização colapsável para reduzir ruído visual em listas longas.
- o formulário expandido do item passou a salvar automaticamente quantidade e preço, sem botão de confirmação.
- a home deixou de exibir ações redundantes e o checkbox do item passou a ocupar posição de maior destaque no card.
- listas abertas passaram a exibir exclusão com confirmação, e campos numéricos deixaram de corrigir o texto prematuramente.
- o detalhe da lista passou a destacar total, total comprado e saldo do orçamento, enquanto a análise visual passou a usar preço informado.

## 2. Pesquisa aplicada em melhores práticas Android

As decisões seguiram principalmente a documentação oficial do Android Developers:

- arquitetura em camadas com separação de responsabilidades;
- UI guiada por estado e dados persistentes;
- `ViewModel` como state holder;
- persistência local com Room como fonte única de verdade;
- Compose declarativo, idempotente e sem side-effects na renderização;
- Material 3 com suporte a tema dinâmico.

## 3. Pesquisa aplicada em código limpo

As convenções de implementação foram alinhadas com as convenções oficiais de Kotlin:

- preferência por `val` e tipos imutáveis;
- nomes explícitos e sem classes “utilitárias” genéricas;
- funções pequenas e com responsabilidade única;
- organização por feature;
- arquivos com nomes semânticos;
- ausência de efeitos colaterais em composables;
- regras de formatação consistentes com Kotlin official style.

## 4. Arquitetura escolhida

### 4.1 Macrovisão

- `core`: modelos e formatação;
- `data`: entidades, DAOs, banco e repositórios;
- `feature`: UI e estado por caso de uso/tela;
- `ui`: tema e navegação.

### 4.2 Justificativa

Essa organização respeita o espírito de “package by feature” da documentação e mantém:

- baixo acoplamento;
- coesão por funcionalidade;
- facilidade de manutenção;
- evolução incremental do app.

## 5. Estratégias aplicadas aos requisitos não funcionais

- Persistência local com Room para robustez offline.
- Consultas e fluxos reativos para atualização imediata da UI.
- Normalização de preço pré-calculada na finalização para leitura simples em análise, considerando a dimensão por unidade.
- Interface Material 3 com feedback visual por `Snackbar`, chips e estados explícitos.
- `minSdk = 29` para compatibilidade com Android 10+.

## 6. Ambiente de desenvolvimento configurado

### 6.1 Ferramentas-alvo

- JDK 17
- Gradle Kotlin DSL
- Android SDK Platform 36
- Android Studio atual com suporte a Compose

### 6.2 Dependências centrais

- Compose BOM
- Material 3
- Navigation Compose
- Room
- Hilt

## 7. Política de commits

O repositório deve seguir Conventional Commits em PT-BR. Exemplos válidos:

- `feat: cria estrutura base do app android`
- `feat: implementa fluxo de listas e finalização`
- `docs: documenta arquitetura e ambiente`
- `fix: corrige atualização de itens na lista`

## 8. Rastreabilidade da mudança do produto

| Data | Tipo | Descrição | Impacto |
| --- | --- | --- | --- |
| 2026-03-08 | Regra de negócio | Inclusão do atributo `dimensão` no item do catálogo | Cadastro de item, snapshot da lista, cálculo de preço base e documentação |
| 2026-03-09 | Regra de negócio | Categorias passaram a ser pré-definidas pelo sistema | Cadastro de item, padronização do catálogo, busca e rastreabilidade do produto |
| 2026-03-09 | Regra de negócio | Itens passaram a suportar compra por medida variável, sem dimensão fixa | Cadastro de item, detalhamento da lista, cálculo de subtotal, normalização de preço e histórico |
| 2026-03-09 | UX e consistência | Itens da lista passaram a ser colapsáveis e a atualização de quantidade/preço tornou-se atômica | Navegação em listas extensas, redução de inconsistências visuais e atualização imediata de subtotal |
| 2026-03-09 | UX e consistência | Edição de itens da lista passou a usar salvamento automático | Menos fricção na edição, persistência automática e redução de ruído por confirmação manual |
| 2026-03-09 | UX e navegação | Home foi simplificada e os cards da lista passaram a expandir ao toque em toda a caixa | Redução de redundância na navegação, leitura mais limpa e interação mais direta com os itens |
| 2026-03-09 | UX e controle | Listas abertas passaram a exibir exclusão com confirmação, e campos numéricos deixaram de reformatar a digitação em andamento | Menos interrupção no fluxo de edição, menor risco de exclusão acidental e ação de limpeza mais direta |
| 2026-03-09 | UX e acompanhamento | O detalhe da lista passou a exibir totais e saldo do orçamento, e a análise visual passou a priorizar preço informado | Melhor acompanhamento da compra em andamento e leitura mais direta do histórico de preço |
