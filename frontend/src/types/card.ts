// Matches backend CardResponse (backend/.../dto/card/CardResponse.java) exactly.
export interface Card {
  id: number;
  term: string; // front of the card
  definition: string; // back of the card
  deckId: number;
  createdAt: string;
}

// Matches backend CardRequest (backend/.../dto/card/CardRequest.java).
export interface CardFormInput {
  term: string;
  definition: string;
}
