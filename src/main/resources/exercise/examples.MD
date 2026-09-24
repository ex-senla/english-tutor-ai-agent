# English Exercise Generation Rules

## 1. General Rules

The exercise generator must receive the following parameters:

### 1.1. English language level

Specify the learner's CEFR level:

* A1
* A2
* B1
* B2
* C1
* C2

### 1.2. Grammar rule

Specify the grammar topic that the exercise should practice.

Examples:

* Present Simple
* Past Simple
* Present Continuous
* Present Perfect
* Future Simple
* Modal Verbs
* Past Modals
* Conditionals
* Passive Voice
* Reported Speech

The specified grammar rule determines what the exercise should test.

### 1.3. Target vocabulary

Provide a list of words or phrases from the learner's personal vocabulary.

The target vocabulary must be naturally incorporated into the exercise sentences.

The target vocabulary is used to create context and practice the learner's vocabulary.

**Important:** Target vocabulary does not determine the correct answer. The exercise must test the specified grammar rule.

### 1.4. General topic

Specify a general topic that should connect all sentences in the exercise.

Examples:

* Autumn
* Space
* Travelling
* The FIFA World Cup
* Technology
* Nature
* Education

All sentences should be relevant to the specified topic.

### 1.5. Number of sentences

Specify the required number of sentences.

The generator must produce exactly the specified number of sentences.

---

# 2. Fill in the Blank

## Specific Rules

### 2.1. The blank must test the specified grammar rule

The missing word or phrase must be directly related to the specified grammar topic.

The learner must apply the specified grammar rule to determine the correct answer.

For example, if the grammar rule is `Past Simple`, the blank should require the correct Past Simple form of a verb.

### 2.2. The learner's vocabulary is used to create context

Words from `targetWords` do not necessarily have to be the missing words.

They must be naturally incorporated into the sentences.

Target words may appear either before or after the blank.

Their purpose is to connect the exercise with the learner's vocabulary and create a natural context for grammar practice.

### 2.3. One blank per sentence

Each sentence must contain exactly one blank.

The blank must be represented as:

`___`

### 2.4. One unambiguous answer

Each sentence must have exactly one clearly correct answer.

The context must not allow multiple answers that are equally correct in terms of both grammar and meaning.

### 2.5. The grammar form must be essential

Do not create sentences where the correct answer can be determined from context alone without applying the specified grammar rule.

The learner must use their knowledge of the grammar rule to determine or form the answer.

## Example

**Level:** A2

**Grammar:** Past Simple

**Topic:** Travelling

**Target words:**

* travel
* mountain
* exciting
* friend

Sentence:

> Last summer, I ___ to the mountains with my best friend.

Answer:

`travelled`

Here, `travel` is a word from the learner's vocabulary and is used in the sentence, while the blank tests the application of `Past Simple`.

---

# 3. Multiple Choice

## Specific Rules

### 3.1. The blank must test the specified grammar rule

The missing word or phrase must be directly related to the specified grammar topic.

The learner must apply the specified grammar rule to choose the correct answer.

For example, if the grammar rule is `Past Simple`, the answer options should require the learner to choose the correct Past Simple form of a verb.

### 3.2. The learner's vocabulary is used to create context

Words from `targetWords` do not necessarily have to be answer options.

They must be naturally incorporated into the sentences.

Target words may appear either before or after the blank.

Their purpose is to connect the exercise with the learner's vocabulary and create a natural context for grammar practice.

### 3.3. One blank per sentence

Each sentence must contain exactly one blank.

The blank must be represented as:

`___`

### 3.4. Exactly four answer options

Each sentence must have exactly four answer options.

Only one option must be correct.

The other three options must be incorrect.

### 3.5. The options must test grammar

Incorrect options must be related to the grammar rule being tested.

They should not be random words that can be easily eliminated based on meaning.

Whenever possible, incorrect options should represent common grammatical mistakes that language learners might make.

### 3.6. One unambiguous correct answer

Each question must have exactly one option that is correct both grammatically and semantically.

The context must not allow multiple equally correct answers.

### 3.7. The grammar form must be essential

Do not create questions where the correct option can be determined from context alone without applying the specified grammar rule.

The learner must use their knowledge of the grammar rule to choose the correct answer.

### 3.8. Distribution of correct answers

The correct answer must not always appear in the same position.

Correct answers should be distributed across options A, B, C and D.

## Example

**Level:** A2

**Grammar:** Past Simple

**Topic:** Travelling

**Target words:**

* travel
* mountain
* exciting
* friend

Sentence:

> Last summer, I ___ to the mountains with my best friend.

A. travel
B. travelled
C. travelling
D. travels

Answer:

`B. travelled`

Here, `travel`, `mountain`, `exciting` and `friend` are part of the learner's vocabulary and are used to create context, while the answer options test the application of `Past Simple`.

---

# 4. Core Principle

For both exercise types, the roles of the input parameters are strictly separated:

**English level**
→ determines the difficulty of the language.

**Grammar rule**
→ determines what the learner must practice and what the correct answer should test.

**Target vocabulary**
→ provides vocabulary that must be naturally used in the sentences and creates context.

**General topic**
→ provides a common context for all sentences.

**Number of sentences**
→ determines exactly how many sentences must be generated.

The exercises must primarily test the specified **grammar rule**, while the learner's **target vocabulary** and the **general topic** are used to create meaningful and natural context.
