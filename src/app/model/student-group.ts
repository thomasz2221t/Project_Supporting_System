import { Group } from './group';
import { Presence } from './presence';
import { Student } from './student';

export interface StudentGroup {
  id?: number;

  mark: number;

  group?: Group;
  student?: Student;
  presenceList?: Presence[];
}
