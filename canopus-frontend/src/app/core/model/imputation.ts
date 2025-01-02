import { ImputationItem } from "./imputation-item";

export class Imputation {
    id!: number;
    date!: string;
    items: Array<ImputationItem> = [];
    total!: number;
}