package com.arsylk.mammonsmite.views;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.arsylk.mammonsmite.DestinyChild.DCDefine;
import com.arsylk.mammonsmite.Live2D.L2DModel;
import com.arsylk.mammonsmite.R;
import com.arsylk.mammonsmite.utils.Define;
import com.arsylk.mammonsmite.utils.Utils;
import org.apache.commons.io.FileUtils;

import java.io.File;

public class SaveModelDialog extends AlertDialog.Builder implements Dialog.OnShowListener {
    private AlertDialog dialog;
    private L2DModel l2DModel;
    private EditText input_name, input_id, input_folder;
    private Utils.Callback callback;

    public SaveModelDialog(Context context, L2DModel l2DModel) {
        super(context);
        this.l2DModel = l2DModel;

        View view = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.dialog_save_model, null);
        input_name = view.findViewById(R.id.input_name);
        input_id = view.findViewById(R.id.input_id);
        input_folder = view.findViewById(R.id.input_folder);


        //model id
        input_id.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!DCDefine.MODEL_ID_PATTERN.matcher(input_id.getText().toString()).matches()) {
                    input_id.setError("Incorrect model id!");
                    input_id.setEnabled(true);
                }else {
                    input_id.setError(null);
                    input_id.setEnabled(false);
                }
            }
        });
        input_id.setText(l2DModel.getModelId());

        //model name
        input_name.setText(l2DModel.getModelName());

        //output folder
        input_folder.setText(l2DModel.getModelName().replace(" ", "_").toLowerCase());

        setTitle("Save model");
        setView(view);
        setCancelable(true);
        setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialog.dismiss();
            }
        });
        setPositiveButton("Save", null);
    }

    public void setOnModelSavedListener(Utils.Callback callback) {
        this.callback = callback;
    }

    public void showDialog() {
        dialog = create();
        dialog.setOnShowListener(this);
        dialog.show();
        dialog.setOnShowListener(this);
    }

    @Override
    public void onShow(DialogInterface dialogInterface) {
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File unpackPath = new File(Define.MODELS_DIRECTORY, input_folder.getText().toString());
                if(unpackPath.exists()) {
                    Toast.makeText(getContext(), "Folder already exists!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(input_id.getError() != null) {
                    Toast.makeText(getContext(), "Incorrect model id!", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    //move to unpacked
                    FileUtils.moveDirectory(l2DModel.getOutput(), unpackPath);

                    //generate _model
                    l2DModel.setOutput(unpackPath);
                    l2DModel.setModelName(input_name.getText().toString());
                    l2DModel.generateModel();

                    //finish dialog
                    Toast.makeText(getContext(), "Saved to: "+unpackPath.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                    if(callback != null) {
                        callback.onCall();
                    }
                    dialog.dismiss();
                }catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
