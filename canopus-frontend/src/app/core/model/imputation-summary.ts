export class ImputationSummary {
  name!: string;
  time!: number;

  public static fromObject(obj: any): ImputationSummary {
    const summaryRef = new ImputationSummary();
    Object.assign(summaryRef, obj);
    return summaryRef;
  }
}
