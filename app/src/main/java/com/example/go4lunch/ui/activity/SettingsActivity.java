package com.example.go4lunch.ui.activity;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.lifecycle.ViewModelProvider;

import com.example.go4lunch.R;
import com.example.go4lunch.ViewModels.CommunicationViewModel;
import com.example.go4lunch.databinding.ActivitySettingsBinding;
import com.example.go4lunch.databinding.ToolbarBinding;

public class SettingsActivity extends BaseActivity {

    private ToolbarBinding toolbarBinding;
    private ActivitySettingsBinding binding;

    protected CommunicationViewModel viewModel;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());// added
        View view = binding.getRoot();

        setContentView(view);

        viewModel = new ViewModelProvider(this).get(CommunicationViewModel.class);

        configureToolbar();

    }

    private void configureToolbar(){
       setSupportActionBar(toolbarBinding.simpleToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }




}
