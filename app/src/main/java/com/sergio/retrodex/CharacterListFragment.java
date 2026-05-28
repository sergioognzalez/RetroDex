package com.sergio.retrodex;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CharacterListFragment extends Fragment
        implements CharacterAdapter.OnCharacterActionListener {

    public static final String ARG_MODE = "mode";
    public static final String MODE_DECADE = "decade";
    public static final String MODE_CATEGORY = "category";

    private DatabaseHelper db;
    private CharacterAdapter adapter;
    private RecyclerView recyclerView;
    private View layoutEmpty;
    private Spinner filterSpinner;
    private Spinner sortSpinner;
    private String mode;
    private String currentFilter = null;
    private boolean sortInitialized;

    public static CharacterListFragment newInstance(String mode) {
        CharacterListFragment fragment = new CharacterListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MODE, mode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mode = getArguments().getString(ARG_MODE, MODE_DECADE);
        }
        db = new DatabaseHelper(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_character_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recycler_characters);
        layoutEmpty = view.findViewById(R.id.layout_empty);
        filterSpinner = view.findViewById(R.id.spinner_filter);
        sortSpinner = view.findViewById(R.id.spinner_sort);

        TextView filterLabel = view.findViewById(R.id.tv_filter_label);
        TextView sortLabel = view.findViewById(R.id.tv_sort_label);

        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        String[] filterEntries;
        if (MODE_DECADE.equals(mode)) {
            filterLabel.setText(R.string.filter_decade_label);
            filterEntries = requireContext().getResources().getStringArray(R.array.decades_array);
        } else {
            filterLabel.setText(R.string.filter_category_label);
            filterEntries = requireContext().getResources().getStringArray(R.array.categories_array);
        }

        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                filterEntries
        );
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(filterAdapter);
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View selectedView, int position, long id) {
                currentFilter = position == 0 ? null : filterEntries[position];
                loadData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        sortLabel.setText(R.string.pref_sort_title);
        setupSortSpinner();

        adapter = new CharacterAdapter(requireContext(), new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);
    }

    private void setupSortSpinner() {
        String[] sortEntries = requireContext().getResources().getStringArray(R.array.pref_sort_entries);
        String[] sortValues = requireContext().getResources().getStringArray(R.array.pref_sort_values);

        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                sortEntries
        );
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(sortAdapter);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        String selectedSort = prefs.getString("sort_order", getString(R.string.pref_sort_default));
        for (int i = 0; i < sortValues.length; i++) {
            if (sortValues[i].equals(selectedSort)) {
                sortSpinner.setSelection(i, false);
                break;
            }
        }

        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String newSort = sortValues[position];
                if (!sortInitialized) {
                    sortInitialized = true;
                    return;
                }

                prefs.edit().putString("sort_order", newSort).apply();
                loadData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        if (adapter == null) return;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        String sortOrder = prefs.getString("sort_order", getString(R.string.pref_sort_default));

        List<Character> list;
        if (currentFilter == null) {
            list = db.getAll(sortOrder);
        } else if (MODE_DECADE.equals(mode)) {
            list = db.getByDecade(currentFilter, sortOrder);
        } else {
            list = db.getByCategory(currentFilter, sortOrder);
        }

        adapter.setCharacters(list);
        recyclerView.setVisibility(list.isEmpty() ? View.GONE : View.VISIBLE);
        layoutEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onClick(Character character) {
        android.content.Intent intent = new android.content.Intent(
                requireContext(), CharacterDetailActivity.class);
        intent.putExtra(CharacterDetailActivity.EXTRA_CHARACTER_ID, character.getId());
        startActivity(intent);
    }

    @Override
    public void onEdit(Character character) {
        android.content.Intent intent = new android.content.Intent(
                requireContext(), AddEditCharacterActivity.class);
        intent.putExtra(AddEditCharacterActivity.EXTRA_CHARACTER_ID, character.getId());
        startActivity(intent);
    }

    @Override
    public void onDelete(Character character) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).confirmDelete(character, this::loadData);
        }
    }

    @Override
    public void onShare(Character character) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).shareCharacter(character);
        }
    }
}
