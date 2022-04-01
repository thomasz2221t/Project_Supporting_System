import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Topic } from 'src/app/model/topic';
import { TopicService } from '../../../service/topic.service';
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { OkDialogComponent } from '../../../ui/dialog/ok-dialog/ok-dialog.component';
import { ErrorDialogComponent } from 'src/app/ui/dialog/error-dialog/error-dialog.component';

@Component({
  selector: 'app-add-topic',
  templateUrl: './add-topic.component.html',
  styleUrls: ['./add-topic.component.scss'],
})
export class AddTopicComponent implements OnInit {
  topic: Topic = {
    topicName: '',
    description: '',
  };
  topicForm: FormGroup;

  constructor(private topicService: TopicService, public dialog: MatDialog) {
    this.topicForm = new FormGroup({
      name: new FormControl('', [Validators.required, Validators.minLength(4)]),
      description: new FormControl(''),
    });
  }

  resetForm() {
    this.name?.setValue('');
    this.description?.setValue('');
    Object.keys(this.topicForm.controls).forEach((key) => {
      this.topicForm.get(key)?.setErrors(null);
    });
    this.topicForm.markAsUntouched();
  }

  onFormSubmit() {
    this.topic.topicName = this.name?.value;
    this.topic.description = this.description?.value;
    this.topicService.createTopic(this.topic).subscribe({
      next: (res) => {
        let topic: Topic = res;
        this.resetForm();
        this.openDialog(topic.topicName);
      },
      error: (err) => this.openErrorDialog(err),
    });
  }

  openDialog(topicName: string) {
    const dialogConfig = new MatDialogConfig();

    dialogConfig.disableClose = false;
    dialogConfig.autoFocus = true;
    dialogConfig.closeOnNavigation = true;

    this.dialog.open(OkDialogComponent, {
      data: {
        title: 'Success!',
        description: `Successfully created topic named: ${topicName}`,
      },
    });
  }
  openErrorDialog(errorMessage: string) {
    const dialogConfig = new MatDialogConfig();

    dialogConfig.disableClose = false;
    dialogConfig.autoFocus = true;
    dialogConfig.closeOnNavigation = true;

    this.dialog.open(ErrorDialogComponent, {
      data: {
        errorMessage: errorMessage,
      },
    });
  }
  get name() {
    return this.topicForm.get('name');
  }

  get description() {
    return this.topicForm.get('description');
  }
  ngOnInit(): void {}
}
