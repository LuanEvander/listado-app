# Relatório do Projeto Listado — Snapshot 1.0.0-alpha

## Resumo

Este relatório apresenta o estado atual do projeto Listado no snapshot `1.0.0-alpha`, produzido como base para verificação de aderência às expectativas iniciais dos stakeholders. O documento mantém uma estrutura acadêmica e técnica alinhada ao relatório original do projeto, mas reflete a solução realmente implementada no estado atual do repositório.

O produto entregue consiste em um aplicativo Android offline, desenvolvido em Kotlin com Jetpack Compose, voltado à gestão de catálogo de itens, planejamento de compras, execução de listas, histórico de compras concluídas e análise de preços. Durante a evolução do projeto, o domínio foi refinado para suportar categorias fixas do sistema, itens com dimensão fixa por unidade, itens vendidos por medida variável, autosave na edição de itens da lista, detalhamento de orçamento e melhorias de navegação e legibilidade.

No momento deste snapshot, os fluxos centrais do produto estão implementados, o build encontra-se validado e a documentação de rastreabilidade está atualizada. A fase alpha, contudo, ainda não contempla suíte estruturada de testes automatizados nem rodada formal de testes com usuários, que permanecem planejadas para a evolução posterior do projeto.

## 1. Introdução

O projeto Listado foi concebido para apoiar o gerenciamento offline de listas de compras, permitindo o cadastro reutilizável de itens, o controle de listas em andamento e o acompanhamento histórico de preços. A documentação inicial do projeto foi analisada para extrair escopo funcional, regras de negócio e artefatos de modelagem.

A partir dessa base, a implementação evoluiu para um aplicativo Android funcional, com persistência local e interface declarativa, capaz de atender ao núcleo dos casos de uso identificados. Este relatório consolida o estado atual do software para evitar defasagem entre a documentação e o produto efetivamente construído.

## 2. Objetivos

### 2.1 Objetivo geral

Consolidar e registrar o estado do snapshot `1.0.0-alpha` do projeto Listado, permitindo avaliar se a solução atual atende ao planejamento inicial e às expectativas preliminares dos stakeholders.

### 2.2 Objetivos específicos

- documentar o escopo funcional já implementado;
- registrar as principais decisões técnicas e de produto incorporadas ao software;
- verificar a aderência entre requisitos elicitados e funcionalidades presentes no projeto;
- identificar limitações ainda assumidas para a fase alpha;
- preparar a transição documental para as futuras fases `1.0.0-beta` e `1.0.0`.

## 3. Escopo e requisitos do produto

Com base na documentação fornecida e nas evoluções aprovadas ao longo do desenvolvimento, o produto atual contempla:

- catálogo de itens reutilizável;
- categorias fixas definidas pelo sistema;
- suporte a itens com dimensão fixa por unidade;
- suporte a itens vendidos por medida variável;
- criação, edição e exclusão de listas abertas;
- acompanhamento de orçamento por lista;
- detalhamento e edição de itens da lista com salvamento automático;
- transição de planejamento para execução da compra;
- conclusão de listas com bloqueio de edição posterior;
- histórico de compras concluídas;
- análises de preço por item com série histórica;
- busca de itens no catálogo e na inclusão em listas.

Os requisitos funcionais elicitados permanecem materializados na documentação técnica e no produto, com cobertura resumida da seguinte forma:

| Requisito | Situação no snapshot alpha | Observação |
| --- | --- | --- |
| RF1-RF2 | Atendido | cadastro, edição e inativação lógica de itens |
| RF3 | Atendido | unidades e cálculo compatíveis com itens fixos e variáveis |
| RF4-RF6 | Atendido | criação, edição e detalhamento de listas abertas |
| RF7-RF8 | Atendido | conclusão de listas e histórico imutável |
| RF9-RF10 | Atendido | análises históricas e métricas por item |
| RF11 | Atendido | busca em catálogo e inclusão em listas |

## 4. Arquitetura e tecnologias adotadas

O projeto foi implementado com foco em separação de responsabilidades, manutenção incremental e aderência às práticas recomendadas para Android.

### 4.1 Stack principal

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- Room
- Hilt
- Gradle Kotlin DSL

### 4.2 Organização arquitetural

A solução está organizada em camadas principais:

- `core`: modelos de domínio e utilitários;
- `data`: banco local, entidades, DAOs e repositórios;
- `feature`: telas e `ViewModel`s por funcionalidade;
- `ui`: navegação e tema do aplicativo.

### 4.3 Ambiente consolidado

O snapshot alpha está preparado para desenvolvimento com:

- Android SDK configurado via `local.properties`;
- build validado com `./gradlew assembleDebug`;
- tarefas de apoio em `.vscode/tasks.json`;
- configurações leves de editor em `.vscode/settings.json`.

## 5. Implementação do snapshot 1.0.0-alpha

### 5.1 Catálogo

O catálogo permite cadastrar, editar e inativar itens. O domínio atual contempla dois modos de compra:

- item com dimensão fixa por unidade comercial;
- item sem dimensão fixa, vendido por medida variável.

Além disso, as categorias deixaram de ser livres e passaram a ser selecionadas a partir de uma lista fixa do sistema, reduzindo inconsistências semânticas.

### 5.2 Listas de compras

As listas abertas podem ser criadas, editadas e excluídas. Cada lista possui:

- nome e descrição;
- orçamento opcional;
- total acumulado;
- total efetivamente comprado;
- indicador de posição em relação ao orçamento.

A tela de detalhe foi refinada para deixar explícito quando o usuário está em fase de planejamento, facilitando o entendimento do estado da compra antes da execução.

### 5.3 Itens da lista

Os itens da lista utilizam snapshots próprios do item do catálogo, preservando o contexto da compra. Os principais comportamentos implementados são:

- card expansível/recolhível;
- edição de quantidade e preço com salvamento automático;
- atualização imediata do subtotal na interface;
- suporte a compra por unidade ou por medida;
- marcação de item comprado;
- remoção de item da lista.

### 5.4 Execução, conclusão e histórico

Uma lista pode ser iniciada, migrando do planejamento para a execução. Ao final, a lista pode ser concluída, desde que exista ao menos um item marcado como comprado. Após a conclusão:

- a lista torna-se imutável;
- itens pendentes podem ser removidos ou preservados conforme decisão do usuário;
- os dados relevantes passam a compor o histórico de preços.

### 5.5 Análises

A tela de análises permite observar o comportamento histórico de preços por item, com seleção de item e intervalo temporal. Embora o domínio ainda mantenha preço normalizado internamente, a interface alpha passou a priorizar o preço informado na compra por ser mais útil para leitura inicial dos stakeholders.

## 6. Evoluções de produto incorporadas

Durante o desenvolvimento, algumas regras foram refinadas em relação ao entendimento original. As principais evoluções absorvidas pelo snapshot alpha foram:

- inclusão do atributo `dimensão` no item;
- adoção de categorias fixas do sistema;
- suporte a itens vendidos por medida variável;
- colapso e expansão de itens em listas extensas;
- atualização atômica de quantidade e preço;
- salvamento automático na edição de itens;
- remoção de redundâncias na home;
- exclusão de listas com confirmação;
- compactação de layouts para facilitar leitura e aceite inicial.

Essas mudanças estão rastreadas na documentação do projeto e foram incorporadas ao comportamento efetivo da aplicação.

## 7. Avaliação de aderência ao planejamento inicial

A análise do estado atual indica que o projeto atende ao núcleo do planejamento inicial. Os principais pontos de aderência observados são:

- persistência offline implementada;
- reaproveitamento de itens do catálogo em múltiplas listas;
- histórico imutável de compras concluídas;
- acompanhamento de orçamento durante planejamento e execução;
- suporte à comparação temporal de preço por item;
- navegação funcional entre dashboard, catálogo, listas, histórico, análises e detalhe de lista.

Do ponto de vista de prontidão, o projeto se mostra suficiente para a fase alpha, pois já permite validação concreta do produto com stakeholders usando fluxos reais de uso.

## 8. Limitações e pendências conhecidas

Embora o snapshot esteja funcionalmente consolidado, algumas frentes permanecem fora do escopo desta etapa:

- ausência de suíte de testes unitários e instrumentados;
- ausência de roteiro formal de aceite executado e anexado ao repositório;
- ausência de validação estruturada com usuários finais;
- refinamento visual ainda restrito ao uso base de Material 3 e ajustes de densidade de layout.

Essas limitações não inviabilizam a fase alpha, mas impedem classificar a solução como beta ou estável neste momento.

## 9. Preparação para as próximas versões

A evolução esperada após o snapshot `1.0.0-alpha` é:

1. avaliação dos stakeholders sobre aderência ao escopo inicial;
2. incorporação de eventuais ajustes funcionais;
3. definição e implementação da estratégia de testes;
4. aceite da fase de testes;
5. transição para `1.0.0-beta`;
6. realização de testes com usuários na fase beta.

Após a validação em beta, a solução poderá caminhar para uma versão `1.0.0` com maior segurança funcional e operacional.

## 10. Conclusão

O projeto Listado encontra-se apto para ser tratado como snapshot `1.0.0-alpha`. O produto já expressa o escopo central definido no planejamento inicial e incorpora as principais decisões de negócio negociadas durante o desenvolvimento.

Ainda existem pendências naturais para as próximas fases, especialmente em testes e validação com usuários, mas o estado atual do software é adequado para a avaliação inicial por stakeholders e para a manutenção da documentação alinhada ao código realmente entregue.

## Referências documentais do projeto

- `docs/relatório-final.pdf`
- `docs/analise-tecnica.md`
- `docs/snapshot-1.0.0-alpha.md`
- `README.md`
